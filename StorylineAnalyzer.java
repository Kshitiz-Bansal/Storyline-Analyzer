import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Iterator;
import java.util.Map;


// use label column of nodes and not the id column (even though both are same)


// DOUBTS :
    // 1) Round vs Truncate     // done
    // 2) raise exceptions?     // done
    // 3) wrong inputs?         // done


public class StorylineAnalyzer {

    static class Graph {
        static long count;
        static HashMap<String, LinkedList<Edge>> adj;   // adj list
        // static HashMap<String, Long> index;             // node --> index
        static HashMap<String, Boolean> visited;
        static HashMap<String, Long> occ_count;         // occurences count
        static ArrayList<Map.Entry<String, Long>> extra;    // used for sorting for rank
        static ArrayList<String> c;       // used to store component
        static ArrayList<ArrayList<String>> store;

        Graph(String[] args) {
            count = 0;
            adj = new HashMap();   // adj list
            // index = new HashMap();             // node --> index
            visited = new HashMap();
            occ_count = new HashMap();         // occurences count
            extra = new ArrayList<>();    // used for sorting for rank
            c = new ArrayList();       // used to store component
            store = new ArrayList<>();
            int c = args.length;
            try {
                if(c != 3) {
                    throw new IOException();
                }
                FileInputStream nodes = new FileInputStream(args[0]);
                Scanner nodes_sc = new Scanner(nodes);
                nodes_sc.nextLine();    // skip header line
                while(nodes_sc.hasNextLine()) {
                    String l = nodes_sc.nextLine();
                    boolean quote = false;
                    for(int curr = 0; curr < l.length(); curr++ ) {
                        char curr_chr = l.charAt(curr);
                        if(curr_chr == '\"') {
                            quote = !quote;
                        } else {
                            if(curr_chr == ',' && !quote) {
                                String node = l.substring(curr+1); // as label lena hai, id nahi
                                node = node.replace("\"", "");
                                LinkedList<Edge> list = new LinkedList<>();
                                adj.put(node, list);
                                visited.put(node, false);
                                count++;
                                break;
                            }
                        }
                    }
                }
                nodes_sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                FileInputStream edges = new FileInputStream(args[1]);
                Scanner edges_sc = new Scanner(edges);
                edges_sc.nextLine();    // skip header line
                while(edges_sc.hasNextLine()) {
                    String l = edges_sc.nextLine();
                    long start = 0;
                    boolean quote = false;
                    ArrayList<String> split = new ArrayList<String>();
                    for(int curr=0; curr < l.length(); curr++) {
                        char curr_chr = l.charAt(curr);
                        if(curr_chr == '\"') {
                            quote = !quote;
                        } else {
                            if(curr_chr == ',' && !quote) {
                                split.add(l.substring((int) start,(int) curr));
                                start = curr + 1;
                            }
                        }
                    }
                    split.add(l.substring((int) start));
                    // now split contains 3 strings // source - target - weight
                    if(split.size() != 3) {
                        throw new IOException();
                        // System.out.println("Theres a problem in the dataset");
                        // return;
                    }
                    String source = split.get(0);
                    source = source.replace("\"", "");
                    String target = split.get(1);
                    target = target.replace("\"", "");
                    long weight = Integer.parseInt(split.get(2));
                    // System.out.println(source + " " + target + " " + weight);
                    Edge e = new Edge(source, target, weight);
                    LinkedList<Edge> list;
                    list = adj.get(source);
                    list.add(e);
                    adj.put(source, list);
                    Edge e_ = new Edge(target, source, weight);
                    LinkedList<Edge> list_;
                    list_ = adj.get(target);
                    list_.add(e_);
                    adj.put(target, list_);
                }
                edges_sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(Map.Entry<String, LinkedList<Edge>> itr : adj.entrySet()) {
                LinkedList<Edge> list;
                list = itr.getValue();
                long x = 0L;
                for(int j=0; j<list.size(); j++) {
                    x += list.get(j).weight;
                }
                occ_count.put(itr.getKey(), x);
            }
        }

        public void average() {
            double x = 0;
            int coun = 0;
            for(Map.Entry<String, LinkedList<Edge>> itr : adj.entrySet()) {
                LinkedList<Edge> list;
                list = itr.getValue();
                x += (list.size());
                coun++;
            }
            if(coun == 0) {
                System.out.println("0.00");
                return;
            }
            x /= (double) coun;
            double rounded_ans = (double) Math.round(x*100)/100;
            System.out.printf("%.2f", rounded_ans);
            System.out.println();   // printing new line by choice (logical asssumption)
            return;
        }

        public void rank() {
            for(Map.Entry<String, Long> itr : occ_count.entrySet()) {
                extra.add(itr);
            }
            mergesort(0, extra.size()-1, 1);
            for(int i=0; i<extra.size(); i++) {
                // System.out.println(extra.get(i).getKey() + " " + extra.get(i).getValue());
                System.out.print(extra.get(i).getKey());
                if(i != extra.size()-1) System.out.print(",");
                // extra.add(itr);
            }
            System.out.println();
        }

        public void independent_storylines_dfs() {
            for(Map.Entry<String, LinkedList<Edge>> itr : adj.entrySet()) {
                String node = itr.getKey();
                if(!visited.get(node)) {
                    c.clear();
                    dfs(node);
                    // sort (c)
                    mergesort(0, c.size()-1, 3);
                    ArrayList<String> x = new ArrayList();
                    for(int i=0; i<c.size(); i++) x.add(c.get(i));
                    store.add(x);
                    // for(int i=0; i<c.size(); i++) {
                    //     System.out.print(c.get(i)+" ");
                    // }
                    // System.out.println();
                }
            }
            // System.out.println();
            mergesort(0, store.size()-1, 2);
            // // System.out.println(store.size());
            for(int i=0; i<store.size(); i++) {
                ArrayList<String> l = store.get(i);
                for(int j=0; j<l.size(); j++) {
                    System.out.print(l.get(j));
                    if(j!=l.size()-1) System.out.print(",");
                }
                System.out.println();
            }
        }

        public void dfs(String n) {
            // System.out.println(n);
            visited.replace(n, true);
            c.add(n);
            LinkedList<Edge> l = adj.get(n);
            for(int i=0; i<l.size(); i++) {
                String to = l.get(i).target;
                if(!visited.get(to)) {
                    dfs(to);
                }
            }
        }

        public void mergesort(long l, long r, int type) {
            if(l < r) {
                long m = (r + l) >>> 1;
                mergesort(l, m, type);
                mergesort(m+1, r, type);
                if(type == 1) merge1(l, m, r);
                else if(type == 2) merge2(l, m, r);
                else if(type == 3) merge3(l, m, r);
            }
        }

        public void merge3(long l, long m, long r) {
            ArrayList<String> s3 = new ArrayList();
            long x1 = l;
            long x2 = m+1;
            while(x1 <= m && x2 <= r) {
                if(compare3(x1, x2)) {
                    s3.add(c.get((int)x1));
                    x1++;
                } else {
                    s3.add(c.get((int)x2));
                    x2++;
                }
            }
            while(x2 <= r) {
                s3.add(c.get((int)x2));
                x2++;
            }
            while(x1 <= m) {
                s3.add(c.get((int)x1));
                x1++;
            }
            long z=0, q=l;
            while(z<s3.size()) {
                c.set((int)q, s3.get((int)z));
                z++;
                q++;
            }
        }

        public boolean compare3(long x1, long x2) {
            String a = c.get((int)x1);
            String b = c.get((int)x2);
            if(a.compareTo(b) >= 0) {
                return true;
            } else {
                return false;
            }
        }

        public void merge2(long l, long m, long r) {
            ArrayList<ArrayList<String>> done = new ArrayList<>();
            long x1 = l;
            long x2 = m+1;
            while(x1 <= m && x2 <= r) {
                if(compare2(x1, x2)) {
                    done.add(store.get((int)x1));
                    x1++;
                } else {
                    done.add(store.get((int)x2));
                    x2++;
                }
            }
            while(x2 <= r) {
                done.add(store.get((int)x2));
                x2++;
            }
            while(x1 <= m) {
                done.add(store.get((int)x1));
                x1++;
            }
            long z=0, q=l;
            while(z<done.size()) {
                store.set((int)q, done.get((int)z));
                z++;
                q++;
            }
        }


        public boolean compare2(long x1, long x2) {
            // true if 1 > 2
            ArrayList<String> s1 = store.get((int)x1);
            ArrayList<String> s2 = store.get((int)x2);
            int l1 = s1.size(), l2 = s2.size();
            if(l1 > l2) {
                return true;
            } else if(l1 == l2) {
                for(int i=0; i<l1; i++) {
                    if(s1.get(i).compareTo(s2.get(i)) > 0) return true;
                    else if(s1.get(i).compareTo(s2.get(i)) < 0) return false;
                }
                return true;
            } else {
                return false;
            }
        }


        public void merge1(long l, long m, long r) {
            ArrayList<Map.Entry<String, Long>> done = new ArrayList<>();
            long x1 = l;
            long x2 = m+1;

            while(x1 <= m && x2 <= r) {
                if(compare1(x1, x2)) {
                // if(comp(extra.get((int)x1), extra.get((int)x2))) {
                    done.add(extra.get((int)x1));
                    x1++;
                } else {
                    done.add(extra.get((int)x2));
                    x2++;
                }
            }

            while(x1 <= m) {
                done.add(extra.get((int)x1));
                x1++;
            }
            while(x2 <= r) {
                done.add(extra.get((int)x2));
                x2++;
            }
            long z=0, q=l;
            while(z<done.size()) {
                extra.set((int)q, done.get((int)z));
                z++;
                q++;
            }
        }

        public boolean compare1(long x1, long x2) {
            // true if x1 > x2
            Map.Entry<String, Long> a1 = extra.get((int)x1);
            Map.Entry<String, Long> a2 = extra.get((int)x2);
            if(a1.getValue() == null || a2.getValue() == null) return false;
            if(a1.getValue() > a2.getValue()) {
                return true;
            // } else if(Objects.equals(a1.getValue(), a2.getValue())) {
            } else if(a1.getValue().equals(a2.getValue())) {
                String s1 = a1.getKey();
                String s2 = a2.getKey();
                if(s1.compareTo(s2) > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public static void main(String[] args) {
        Graph g = new Graph(args);
        if(args[2].equals("average")) {
            g.average();
        } else if(args[2].equals("rank")) {
            g.rank();
        } else if(args[2].equals("independent_storylines_dfs")) {
            g.independent_storylines_dfs();
        } else {
            System.out.println("Cant you give correct input?!");
            return;
        }
    }
}


class Edge {
    String source;
    String target;
    long weight;

    public Edge(String s, String t, long w) {
        this.source = s;
        this.target = t;
        this.weight = w;
    }
}
