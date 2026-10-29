package com.example.util;

import org.hibernate.Session;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;

public class PerformanceReport {

    public static void printReport(EntityManager em) {
        Session session = em.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();

        long hits = stats.getSecondLevelCacheHitCount();
        long misses = stats.getSecondLevelCacheMissCount();
        long puts = stats.getSecondLevelCachePutCount();
        long queries = stats.getQueryExecutionCount();
        long queryTime = stats.getQueryExecutionMaxTime();
        long txCount = stats.getTransactionCount();

        double hitRatio = (hits + misses > 0) ? ((double) hits / (hits + misses) * 100.0) : 0.0;

        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("                 RAPPORT DE PERFORMANCE HIBERNATE / JPA                 ");
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("  * Transactions executees           : %d%n", txCount);
        System.out.printf("  * Requetes SQL executees           : %d%n", queries);
        System.out.printf("  * Temps max d'execution requete    : %d ms%n", queryTime);
        System.out.printf("  * Cache L2 Hits (Succes)           : %d%n", hits);
        System.out.printf("  * Cache L2 Misses (Echecs)         : %d%n", misses);
        System.out.printf("  * Cache L2 Puts (Enregistrements)  : %d%n", puts);
        System.out.printf("  * Taux de succes Cache L2 (Ratio)  : %.2f %%%n", hitRatio);
        System.out.println("------------------------------------------------------------------------");

        System.out.println("  [MOTEUR DE RECOMMANDATIONS D'OPTIMISATION]");
        if (queries > 10) {
            System.out.println("  -> ALERTE : Volume de requetes eleve detecte. Recommandation : Utiliser JOIN FETCH ou Entity Graph pour eviter le probleme N+1.");
        } else {
            System.out.println("  -> OPTIMAL : Volume de requetes controle. Les associations principales utilisent des jointures efficaces.");
        }

        if (hitRatio > 50.0) {
            System.out.println("  -> EXCELLENT : Le cache de second niveau (Ehcache) absorbe efficacement les lectures repetitives.");
        } else {
            System.out.println("  -> CONSEIL : Continuer a alimenter le cache L2 sur les entites a forte frequence de lecture (Salles, Equipements).");
        }
        System.out.println("------------------------------------------------------------------------");
    }
}
