package storm.starter.trident;

import java.util.UUID;

import storm.trident.spout.IBatchSpout;
import storm.trident.spout.ITridentSpout;
import storm.trident.testing.FixedBatchSpout;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.trident.Stream;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.FilterNull;
import storm.trident.operation.builtin.MapGet;
import storm.trident.operation.builtin.Sum;
import storm.trident.planner.processor.StateQueryProcessor;
import storm.trident.testing.MemoryMapState;
import storm.trident.tuple.TridentTuple;


public class TridentWordCount {    
    public static class Split extends BaseFunction {
        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            String sentence = tuple.getString(0);
            for(String word: sentence.split(" ")) {
                collector.emit(new Values(word));                
            }
        }
    }
    
    public static class SentenceIdGenerator extends BaseFunction {
        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
        	collector.emit(new Values(UUID.randomUUID()));                
        }
    }
    
    public static class PrintlnFunction extends BaseFunction {
        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            System.out.println("New Tuple for printing: " + tuple.toString());
            collector.emit(new Values("dummy"));
        }
    }
    
    public static TridentTopology buildTopology(ITridentSpout spout, LocalDRPC drpc) {
    	TridentTopology topology = new TridentTopology(); 
    	Stream sentenceStream = null;
    	
    	if(spout == null){
    		FixedBatchSpout fixedSpout = new FixedBatchSpout(new Fields("sentence"), 3,
                new Values("the cow jumped over the moon"),
                new Values("the man went to the store and bought some candy"),
                new Values("four score and seven years ago"),
                new Values("how many apples can you eat"),
                new Values("to be or not to be the person"));
        	((FixedBatchSpout)fixedSpout).setCycle(true);
        	sentenceStream = topology.newStream("spout1", fixedSpout)
            	.parallelismHint(16)
            	.each(new Fields("sentence"), new SentenceIdGenerator(), new Fields("sentenceId"));
    	} else {
    		sentenceStream = topology.newStream("spout1", spout)
                	.parallelismHint(16)
                	.each(new Fields("sentence"), new SentenceIdGenerator(), new Fields("sentenceId"));
    	}
    	
    	Stream wordStream = sentenceStream.each(new Fields("sentence"), new Split(), new Fields("word"));
        
    	TridentState wordCounts = wordStream
                .groupBy(new Fields("word"))
                .persistentAggregate(new MemoryMapState.Factory(),
                                     new Count(), new Fields("count"))         
                .parallelismHint(16);
    	
    	TridentState localWordCounts = wordStream
                .groupBy(new Fields("sentenceId","word"))
                .persistentAggregate(new MemoryMapState.Factory(),
                                     new Count(), new Fields("localCount"))         
                .parallelismHint(16);
    	
    	//Stream batchTotalStream = wordStream.groupBy(new Fields("word"))
    	//							.aggregate(new Fields("word"), new Count(), new Fields("localTotal"));
    								//.each(new Fields("word", "localTotal"), new PrintlnFunction(), new Fields());
                
        topology.newDRPCStream("words", drpc)
                .each(new Fields("args"), new Split(), new Fields("word"))
                .groupBy(new Fields("word"))
                .stateQuery(wordCounts, new Fields("word"), new MapGet(), new Fields("count"))
                .each(new Fields("count"), new FilterNull())
                .aggregate(new Fields("count"), new Sum(), new Fields("sum"))
                ;
        
        Stream globalTotalStream = wordCounts.newValuesStream();
        							//.each(new Fields("word", "count"), new PrintlnFunction(), new Fields());
        
        Stream localTotalStream = localWordCounts.newValuesStream()
        		.project(new Fields("word","localCount"));
				//.each(new Fields("word", "localCount"), new PrintlnFunction(), new Fields());
        
        topology.join(globalTotalStream, new Fields("word"), localTotalStream , new Fields("word"),new Fields("word","count","localCount"))
        				.each(new Fields("word","count", "localCount"), new PrintlnFunction(), new Fields());
        
        return topology;
    }
    
    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        conf.setMaxSpoutPending(20);
        if(args.length==0) {
            LocalDRPC drpc = new LocalDRPC();
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("wordCounter", conf, buildTopology(null, drpc).build());
            for(int i=0; i<100; i++) {
                System.out.println("DRPC RESULT: " + drpc.execute("words", "cat the dog jumped"));
                Thread.sleep(1000);
            }
        } else {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopology(args[0], conf, buildTopology(null, null).build());        
        }
    }
}
