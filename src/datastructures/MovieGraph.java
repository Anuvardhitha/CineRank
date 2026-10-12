package datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class MovieGraph {

    private HashMap<Integer, List<Integer>> graph;

    public MovieGraph() {

        graph = new HashMap<Integer, List<Integer>>();
    }

    public void addMovie(int movieId) {

        if (!graph.containsKey(movieId)) {

            graph.put(
                movieId,
                new ArrayList<Integer>()
            );
        }
    }

    public void addConnection(
            int movieId1,
            int movieId2) {

        addMovie(movieId1);
        addMovie(movieId2);

        graph.get(movieId1).add(movieId2);
        graph.get(movieId2).add(movieId1);
    }

    public List<Integer> getSimilarMovies(int movieId) {

        if (!graph.containsKey(movieId)) {

            return new ArrayList<Integer>();
        }

        return graph.get(movieId);
    }

    public List<Integer> bfs(int startMovieId) {

        List<Integer> result =
            new ArrayList<Integer>();

        if (!graph.containsKey(startMovieId)) {
            return result;
        }

        Queue<Integer> queue =
            new LinkedList<Integer>();

        Set<Integer> visited =
            new HashSet<Integer>();

        queue.offer(startMovieId);
        visited.add(startMovieId);

        while (!queue.isEmpty()) {

            int currentMovie =
                queue.poll();

            result.add(currentMovie);

            List<Integer> neighbors =
                graph.get(currentMovie);

            for (Integer neighbor : neighbors) {

                if (!visited.contains(neighbor)) {

                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return result;
    }
}