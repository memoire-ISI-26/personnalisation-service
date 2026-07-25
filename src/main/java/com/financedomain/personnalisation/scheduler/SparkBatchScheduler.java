package com.financedomain.personnalisation.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

@Component
public class SparkBatchScheduler {

    // S'exécute par défaut toutes les 2 heures (configurable dans les properties via spark.batch.cron)
    @Scheduled(cron = "${spark.batch.cron:0 0 */2 * * *}")
    public void runSparkBatchJob() {
        System.out.println("==================================================");
        System.out.println("[SCHEDULER] Déclenchement automatique du Job Spark Batch...");
        System.out.println("==================================================");

        try {
            // Commande spark-submit
            /*ProcessBuilder pb = new ProcessBuilder(
                "spark-submit",
                "--class", "MainExportApi",
                "--master", "local[*]", "--packages org.apache.spark:spark-sql-kafka-0-10_2.13:4.1.1",
                "F:\\Master2\\Memoire\\personnalisation\\target\\out\\jvm\\scala-2.13.17\\api-spark-scala\\api-spark-scala_2.13-1.0.jar"
            );*/
            ProcessBuilder pb = new ProcessBuilder(
                "spark-submit",
                "--class", "MainStreamingPersonalization",
                "--master", "local[*]", "--packages org.apache.spark:spark-sql-kafka-0-10_2.13:4.1.1",
                "F:\\Master2\\Memoire\\personnalisation\\target\\out\\jvm\\scala-2.13.17\\api-spark-scala\\api-spark-scala_2.13-1.0.jar"
            );

            // Rediriger le flux d'erreur standard vers le flux d'entrée standard
            pb.redirectErrorStream(true);

            // Démarrage du processus
            Process process = pb.start();

            // Lecture de la sortie de Spark
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[SPARK-BATCH] " + line);
                }
            }

            // Attente de la fin du processus
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("[SCHEDULER] Job Spark Batch exécuté avec succès (exit code: 0).");
            } else {
                System.err.println("[SCHEDULER] Le Job Spark Batch a échoué avec le code de sortie : " + exitCode);
            }

        } catch (IOException e) {
            System.err.println("[SCHEDULER] Erreur E/S lors de l'exécution de spark-submit : " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("[SCHEDULER] L'exécution du Job Spark Batch a été interrompue : " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        System.out.println("==================================================");
    }
}
