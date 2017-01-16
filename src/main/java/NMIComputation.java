import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.collections.CollectionUtils.intersection;

/**
 * Created by steve on 16/01/2017.
 */
public class NMIComputation {
    public static void main(String[] args) {
        String[] fields = {"hashtag", "url", "mention", "retweet"};
        String[] similarityMetrics = {"cosine", "jaccard"};
        String inputPrefix = "../../data/modularities/";

        HashMap<String, Double> nmiMap = new HashMap<>();

        for (String field : fields) {
            ArrayList<HashMap<Integer, ArrayList<String>>> modularityMaps = new ArrayList<>();
            for (String similarityMetric : similarityMetrics) {
                // load graph for specific field and similarity
                modularityMaps.add(loadFromCsv(inputPrefix+similarityMetric+"_modularity_"+field+".csv"));
            }
            HashMap<Integer, ArrayList<String>> cA = modularityMaps.get(0);
            HashMap<Integer, ArrayList<String>> cB = modularityMaps.get(1);
            int N = 0;
            for (ArrayList<String> i : cA.values()) {
                N += i.size();
            }

            double accNumerator = 0.0;
            double accDenominatorA = 0.0;
            double accDenominatorB = 0.0;
            for(Map.Entry<Integer, ArrayList<String>> i : cA.entrySet()) {
                HashSet<String> iDistinct = new HashSet<>();
                iDistinct.addAll(i.getValue());
                int Ni = iDistinct.size();
                for(Map.Entry<Integer, ArrayList<String>> j : cB.entrySet()) {
                    HashSet<String> jDistinct = new HashSet<>();
                    jDistinct.addAll(j.getValue());
                    HashSet<String> ij = new HashSet<>(iDistinct);
                    ij.retainAll(jDistinct);
                    int Nij = ij.size();
                    int Nj = jDistinct.size();
                    accNumerator += Nij*Math.log(Nij*N/(Ni*Nj));
                }
                accDenominatorA += Ni*Math.log(Ni/N);
            }

            for(Map.Entry<Integer, ArrayList<String>> j : cB.entrySet()) {
                HashSet<String> jDistinct = new HashSet<>();
                jDistinct.addAll(j.getValue());
                int Nj = jDistinct.size();
                accDenominatorB += Nj*Math.log(Nj/N);
            }
            // compare graphs for specific field (compute NMI)
            double nmi = -2*accNumerator/(accDenominatorA+accDenominatorB);
            nmiMap.put(field, nmi);
        }

        for(Map.Entry<String, Double> s : nmiMap.entrySet()) {
            System.out.printf("%s NMI = %f\n", s.getKey(), s.getValue());
        }
    }

    static public HashMap<Integer, ArrayList<String>> loadFromCsv(String filename) {
        HashMap<Integer, ArrayList<String>> toBeReturned = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            try {
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null){
                    String[] tokens = line.split(",");
                    ArrayList<String> l = toBeReturned.get(Integer.parseInt(tokens[1]));
                    if (l == null) {
                        l = new ArrayList<>();
                    }
                    l.add(tokens[0]);
                    toBeReturned.put(Integer.parseInt(tokens[1]), l);
                }
            }
            catch(IOException e) {
                System.err.println(e.getMessage());
            }
        }
        catch (FileNotFoundException e){
            System.err.println(e.getMessage());
        }

        return toBeReturned;
    }
}
