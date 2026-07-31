package com.financedomain.personnalisation.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

@Slf4j
@Component
@ConditionalOnProperty(name = "spark.batch.enabled", havingValue = "true")
public class SparkBatchScheduler {

    @Value("${spark.submit.path:spark-submit}")
    private String sparkSubmitExecutable;

    @Value("${spark.jar.path:F:\\Master2\\Memoire\\personnalisation\\target\\out\\jvm\\scala-2.13.17\\api-spark-scala\\api-spark-scala_2.13-1.0.jar}")
    private String sparkJarPath;

    // S'exécute par défaut toutes les 2 heures (configurable dans les properties via spark.batch.cron)
    @Scheduled(cron = "${spark.batch.cron:0 0 */2 * * *}")
    public void runSparkBatchJob() {

        log.info("[SCHEDULER] Déclenchement automatique du Job Spark Batch avec l'exécutable: {}", sparkSubmitExecutable);

        try {
            // Commande spark-submit sécurisée et configurable
            ProcessBuilder pb = new ProcessBuilder(
                sparkSubmitExecutable,
                "--class", "MainStreamingPersonalization",
                "--master", "local[*]",
                "--packages", "org.apache.spark:spark-sql-kafka-0-10_2.13:4.1.1",
                sparkJarPath
            );

            // Rediriger le flux d'erreur standard vers le flux d'entrée standard
            pb.redirectErrorStream(true);

            // Démarrage du processus
            Process process = pb.start();

            // Lecture de la sortie de Spark
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[SPARK-BATCH] {}", line);
                }
            }

            // Attente de la fin du processus
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("[SCHEDULER] Job Spark Batch exécuté avec succès (exit code: 0).");
            } else {
                log.error("[SCHEDULER] Le Job Spark Batch a échoué avec le code de sortie : {}", exitCode);
            }

        } catch (IOException e) {
            log.error("[SCHEDULER] Erreur E/S lors de l'exécution de spark-submit : {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error("[SCHEDULER] L'exécution du Job Spark Batch a été interrompue : {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        log.info("==================================================");
    }
}
