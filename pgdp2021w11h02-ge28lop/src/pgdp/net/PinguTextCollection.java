package pgdp.net;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PinguTextCollection {
    private Map<Long, PinguText> map = new HashMap<Long, PinguText>();
    private long id = 0;

    public PinguTextCollection() {
    }

    public synchronized PinguText add(String title, String author, String text) {
        PinguText pinguTextToHandle = new PinguText(id, title, author, text);
        map.put(id, pinguTextToHandle);
        id++;
        return pinguTextToHandle;
    }

    public synchronized PinguText findById(long id) {
        if (map.containsKey(id))
            return map.get(id);
        return null;
    }

    public synchronized List<PinguText> getAll() {
        return map.entrySet().stream()
                .sorted((x, y) -> x.getKey().compareTo(y.getKey()))
                .map(x -> x.getValue())
                .collect(Collectors.toList());
    }

    public synchronized Map<PinguText, Double> findPlagiarismFor(long id) {

        final class PinguTextAndDouble {
            private PinguText pinguText;
            private double similarity;

            PinguTextAndDouble(PinguText pinguText, double similarity) {
                this.pinguText = pinguText;
                this.similarity = similarity;
            }

            double getSimilarity() {
                return similarity;
            }

            PinguText getPinguText() {
                return pinguText;
            }
        }

        if (!map.containsKey(id))
            return null;

        PinguText toHandle = map.get(id);

        List<PinguText> list = getAll();
        return list.stream()
                .filter(x -> x.getId() != id)
                .map(x -> new PinguTextAndDouble(x, x.computeSimilarity(toHandle)))
                .filter(x -> x.getSimilarity() >= 0.001)
                .collect(Collectors.toMap(PinguTextAndDouble::getPinguText, PinguTextAndDouble::getSimilarity));
    }

    /*public static void main(String[] args) {

        class PinguTextAndDouble {
            PinguText pinguText;
            double similarity;

            PinguTextAndDouble(PinguText pinguText, double similarity) {
                this.pinguText = pinguText;
                this.similarity = similarity;
            }

            double getSimilarity() {
                return similarity;
            }

            PinguText getPinguText() {
                return pinguText;
            }
        }

        PinguTextCollection col = new PinguTextCollection();
        col.add("fisch", "byFischi", "sdfajsdflsfashdfkshdfklhdfkjhaslkfj");
        col.add("fisch", "byFischi", "sdfajsdflashdfkshdfklhdfkjhaslkfj");
        col.add("lsdfsdfljsdflshdfkjeo", "bkljhkhkljhklhlhkjyleklho", "fflalsdjfaklsdfhlkakljhsdfklhadskfjlhasdfkljhasklfhakdjsfhaskljdfhaksldfhakjsdfhakljdfhakfjahsdlkfhakjsdfhakljsfhkljsadflkahfalkshfdklasjhdflkajdhfkajsdhfkjashflakjhdflkjahsfkalkshflkahsdflsjhdfkjhasdf");

        List<PinguText> l = col.getAll();

        for (int i = 0; i < l.size(); ++i) {
            PinguText toHandle = l.get(i);
            System.out.println(toHandle.getId());
            System.out.println(toHandle.getTitle());
            System.out.println(toHandle.getAuthor());
            System.out.println(toHandle.getText());
        }

        System.out.println();

        Map<PinguText, Double> map = col.findPlagiarismFor(1);
        if(map == null)
            System.out.println("This is correct my friend!");

        List<PinguTextAndDouble> list = map.entrySet().stream()
                .map(x -> new PinguTextAndDouble(x.getKey(), x.getValue()))
                .collect(Collectors.toList());

        for(int i = 0; i < list.size(); ++i){
            PinguTextAndDouble toHandle = list.get(i);
            System.out.println(toHandle.pinguText.getId());
            System.out.println(toHandle.pinguText.getTitle());
            System.out.println(toHandle.pinguText.getAuthor());
            System.out.println(toHandle.pinguText.getTitle());
            System.out.println(toHandle.getSimilarity());
            System.out.println();
        }
    }*/


}
