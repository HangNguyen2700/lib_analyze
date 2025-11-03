package org.example;
import java.util.*;

public class MapDemo {

        // Map<Integer, Set<Integer>>
        private static final Map<Integer, Set<Integer>> map = new HashMap<>();



        // Add one or more values to the set at a key
//        static void add(Integer key, Integer... values) {
//            get(key).addAll(Arrays.asList(values));
//        }

        // Pretty print the map (sorted sets for readability)
        static void printMap() {
            System.out.println("Current map contents:");
            map.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> {
                        Set<Integer> sorted = new TreeSet<>(e.getValue());
                        System.out.println("  " + e.getKey() + " -> " + sorted);
                    });
        }

        public static void main(String[] args) {
            // Mock keys/values
//            Integer int1 = 1, int11 = 11, int12 = 12, int13 = 13;
//            Integer int2 = 2, int21 = 21, int22 = 22, int23 = 23;
//
//            // Create the bucket for key=1 and add elements
//            map.computeIfAbsent(int1, k -> new HashSet<>());
//            Set<Integer> value = map.get(int1);   // returns Set<Integer> for key=1
//            value.add(int11);
//            value.add(int12);
//
//            // Add more elements in turn
//            value.add(int13);                 // still the same bucket (key=1)
//            map.computeIfAbsent(int2, k -> new HashSet<>());
//            Set<Integer> value2 = map.get(int2);
//            value2.add(int11);
//            value2.add(int12);
//
//            // Print results
//            printMap();
            String str1 = "opensymphony:webwork:1.4";
            String str2 = "opensymphony:webwork:1.4";
            System.out.println(str1.equals(str2));

        }


}
