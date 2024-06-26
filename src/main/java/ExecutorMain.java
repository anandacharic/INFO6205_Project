import GUI.TSP_GUI;
import GUI.Vertex;
import MSTAlgorithms.*;
import OptimizationAlgorithms.AntColonyOptimization;
import OptimizationAlgorithms.RandomSwap;
import OptimizationAlgorithms.SimulatedAnnealing;
import OptimizationAlgorithms.TwoOpt;
import Utils.AntColonyData;
import Utils.CostTraversal;
import Utils.Data;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static Utils.ReadingData.FetchFile;

public class ExecutorMain
{
    public static void main(String args[])
    {
        //Read the file from the path
        String filePath = "info6205.spring2023.teamproject.csv";
        List<Data> da = FetchFile(filePath);

        //Creating the graph
        double graph[][] = new double[da.size()][da.size()];

        System.out.println(da.size()+"  "+"size");

        //Initialization for the graph matrix
        for (int i = 0; i < da.size(); i++)
            for (int j = 0; j < da.size(); j++)
                graph[i][j] = Utils.HaversineDistance.distance(da.get(i), da.get(j));

        //Using Prims to find the shortest path
        Prims pr =new Prims();
        List<Edge> li=pr.PrimsAlgorithms(graph);

        //Finding the number of odd Vertices
        OddVertices ov=new OddVertices();
        List<Integer> oddVert = ov.OddVertex(li, graph.length);
        double oddVertices[][]=ov.oddVertexCostMatrix(oddVert,graph);

        //using the Hungarian Algorithm to find the order of execution
        Hungarian hungarian=new Hungarian(oddVertices);
        int orderexecture[] = hungarian.execute();
        List<Edge> matching=hungarian.matching2(oddVert,orderexecture,graph,li);

        //Merging the two oddvertices and mst
        List<Edge> hungarianmatch = new ArrayList<>(li);
        hungarianmatch.addAll(matching);

        //EulerianCircuit will give us a cycle from one index to another
        EulerianCircuit ec = new EulerianCircuit(hungarianmatch, graph.length);
        List<Integer> ecircuit= ec.EulerianCircuitAlgorithm();

        //HamiltonianCircuit for finding the edges wihtout dupicate vertexes
        List<Integer> hamiltonianCircuit = new ArrayList<>();
        boolean[] visited = new boolean[graph.length];
        int prev=0;
        visited[0]=true;
        hamiltonianCircuit.add(prev);
        for (int i = 1; i < ecircuit.size(); i++) {
            int vertex = ecircuit.get(i);
            if (!visited[vertex]) {
                visited[vertex] = true;
                hamiltonianCircuit.add(vertex);
            }
        }
        hamiltonianCircuit.add(0);
        List<Double> costAlgo=new ArrayList<>();

        double cost=0.0;
        CostTraversal costTraversal=new CostTraversal();
        cost=costTraversal.costTraversal(hamiltonianCircuit,graph.length,graph,hamiltonianCircuit.get(0));
        costAlgo.add(cost);

        //RandomSwaping
        RandomSwap randomSwap=new RandomSwap();
        List<Integer> newtours = randomSwap.RandomSwapOpt(hamiltonianCircuit,graph,1000000);
        cost=costTraversal.costTraversal(newtours,graph.length,graph,newtours.get(0));
        costAlgo.add(cost);
        for(int i=0;i<newtours.size();i++)
            System.out.println(da.get(newtours.get(i)).getIds());
        System.out.println("Random Swaping ....."+costAlgo.get(1));

        //Two Opt
        TwoOpt twoOpt=new TwoOpt();
        List<Integer> tOpt = twoOpt.TwoOptAlgorithm(hamiltonianCircuit,graph);
        cost=costTraversal.costTraversal(tOpt,graph.length,graph,tOpt.get(0));
        costAlgo.add(cost);
        for(int i=0;i<tOpt.size();i++)
            System.out.println(da.get(tOpt.get(i)).getIds());
        System.out.println("2 Opt ....."+costAlgo.get(2));
        System.out.println("Working on Simulated Annealing, This takes time.......");


        //Simulated Annealing
        SimulatedAnnealing simulatedAnnealing=new SimulatedAnnealing();
        List<Integer> simulatedTour = simulatedAnnealing.simulatedAnnealing(tOpt,graph,1);
        cost=costTraversal.costTraversal(simulatedTour,graph.length,graph,simulatedTour.get(0));
        costAlgo.add(cost);
        for(int i=0;i<simulatedTour.size();i++)
            System.out.println(da.get(simulatedTour.get(i)).getIds());
        System.out.println("Simulated Annealing ....."+costAlgo.get(3));
        System.out.println("Working on Ant Colony, this takes time.......");


        //Ant Colony
        AntColonyData acd = new AntColonyData(graph);
        acd.initialize_pheromone();
        AntColonyOptimization aco = new AntColonyOptimization(acd.getPheromone(), acd.getNumAnts(), acd.getNumIterations(),
                acd.getAlpha(), acd.getBeta(), acd.getEvaporation(), acd.getQ(), graph, da.size());
        aco.solve();
        List<Integer> antcolony = aco.getBestTour();
        cost = costTraversal.costTraversal(antcolony, graph.length, graph, antcolony.get(0));
        costAlgo.add(cost);
        for(int i=0;i<antcolony.size();i++)
            System.out.println(da.get(antcolony.get(i)).getIds());
        System.out.println("Ant Colony ....."+costAlgo.get(4));



        List<Double> xAxis=new ArrayList<>();
        List<Double> yAxis=new ArrayList<>();

        Random random = new Random();
        double rn1=0.0;
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)(size.getWidth()*0.80);
        int height = (int)(size.getHeight()*0.95);

        for (int i = 0; i<tOpt.size()-1; i++) {
            rn1 = random.nextDouble(10, width);
            if(!xAxis.contains(rn1))
                xAxis.add(rn1);
        }

        for (int i = 0; i<tOpt.size()-1; i++) {
            rn1 = random.nextDouble(10, height);
            if(!yAxis.contains(rn1))
                yAxis.add(rn1);
        }

        List<Vertex> vertexLists=new ArrayList<>();

        for(int i=0;i<tOpt.size()-1;i++){
            vertexLists.add(new Vertex(xAxis.get(i),yAxis.get(i),da.get(tOpt.get(i)).getIds()));
        }

        vertexLists.add(new Vertex(xAxis.get(0),yAxis.get(0),da.get(tOpt.get(0)).getIds()));

        TSP_GUI tsp_gui=new TSP_GUI(vertexLists,hamiltonianCircuit,newtours,tOpt,simulatedTour,antcolony,costAlgo);


    }
}
