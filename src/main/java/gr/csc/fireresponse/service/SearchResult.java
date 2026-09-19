package gr.csc.fireresponse.service;

/**
 * The outcome of a search: where the item was found (or -1) and how many comparisons it took.
 * Counting comparisons makes the cost of each algorithm visible to learners.
 */
public class SearchResult {

    private final int index;
    private final int comparisons;

    public SearchResult(int index, int comparisons) {
        this.index = index;
        this.comparisons = comparisons;
    }

    public int getIndex() {
        return index;
    }

    public int getComparisons() {
        return comparisons;
    }

    public boolean isFound() {
        return index >= 0;
    }
}
