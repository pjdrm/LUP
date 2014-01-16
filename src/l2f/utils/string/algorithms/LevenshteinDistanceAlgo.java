package l2f.utils.string.algorithms;

public class LevenshteinDistanceAlgo {

    //****************************
    // Get minimum of three values
    //****************************
    private static int Minimum(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    //*****************************
    // Compute Levenshtein distance
    //*****************************
    public static int LD(String st, String tt) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        String s_i; // ith character of s
        String t_j; // jth character of t
        int cost; // cost

        // Step 1

        final String[] sentence_1 = st.split(" ");
        final String[] sentence_2 = tt.split(" ");

        n = sentence_1.length;
        m = sentence_2.length;
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2

        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3

        for (i = 1; i <= n; i++) {

            s_i = sentence_1[i - 1];

            // Step 4

            for (j = 1; j <= m; j++) {

                t_j = sentence_2[j - 1];

                // Step 5

                if (s_i.equalsIgnoreCase(t_j)) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                // Step 6

                d[i][j] = Minimum(d[i - 1][j], d[i][j - 1] + 1, d[i - 1][j - 1] + cost);

            }

        }

        // Step 7

        return d[n][m];

    }

    public static double LDProbability(String st, String tt) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        String s_i; // ith character of s
        String t_j; // jth character of t
        int cost; // cost

        // Step 1

        String[] sentence_1;
        String[] sentence_2;
        if (tt.split(" +").length > st.split(" +").length) {
            sentence_1 = st.split(" +");
            sentence_2 = tt.split(" +");
        } else {
            sentence_1 = tt.split(" +");
            sentence_2 = st.split(" +");
        }


        /* String[] sentence_1 = st.split(" ");
        String[] sentence_2 = tt.split(" ");*/

        n = sentence_1.length;
        m = sentence_2.length;
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2

        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3

        for (i = 1; i <= n; i++) {

            s_i = sentence_1[i - 1];

            // Step 4

            for (j = 1; j <= m; j++) {

                t_j = sentence_2[j - 1];

                // Step 5

                if (s_i.equalsIgnoreCase(t_j)) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                // Step 6

                d[i][j] = Minimum(d[i - 1][j], d[i][j - 1] + 1, d[i - 1][j - 1] + cost);

            }

        }

        // Step 7
        return 1.0 - ( (float) d[n][m] / (float) sentence_2.length );
        //return (1.0 /(1.0 + Math.pow(d[n][m],1.0/4.0)));

    }

}
