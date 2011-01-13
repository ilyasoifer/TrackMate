package fiji.plugin.trackmate.tracking.test;

import ij.ImagePlus;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import mpicbg.imglib.util.Util;

import org.jdom.DataConversionException;
import org.jdom.JDOMException;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import fiji.plugin.trackmate.Feature;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.tracking.LAPTracker;
import fiji.plugin.trackmate.tracking.TrackerSettings;
import fiji.plugin.trackmate.tracking.TrackerType;
import fiji.plugin.trackmate.visualization.HyperStackDisplayer;
import fiji.plugin.trackmate.visualization.SpotDisplayer;

public class LAPTrackerTestDrive {
	
	private static final String FILE_NAME_1 = "Test1.xml";
	private static final String FILE_NAME_2 = "Test2.xml";

	@SuppressWarnings("unused")
	private static final File SPLITTING_CASE_1 = new File(LAPTrackerTestDrive.class.getResource(FILE_NAME_1).getFile());
	private static final File SPLITTING_CASE_2 = new File(LAPTrackerTestDrive.class.getResource(FILE_NAME_2).getFile());
	
	/*
	 * MAIN METHOD
	 */
	
	public static void main(String args[]) {
		
		File file = SPLITTING_CASE_2;
		
		// 1 - Load test spots
		System.out.println("Opening file: "+file.getAbsolutePath());		
		TmXmlReader reader = new TmXmlReader(file);
		// Parse
		try {
			reader.parse();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// All spots
		Settings inFileSettings = null;
		TreeMap<Integer, List<Spot>> spots = null;
		try {
			spots = reader.getAllSpots();
			inFileSettings = reader.getSettings();
		} catch (DataConversionException e) {
			e.printStackTrace();
		}
		
		// 1.5 - Set the tracking settings
		TrackerSettings settings = new TrackerSettings();
		settings.trackerType = TrackerType.LAP_TRACKER;
		settings.allowGapClosing = false; //true;
		settings.gapClosingDistanceCutoff = 100;
		settings.gapClosingTimeCutoff = 10;
		settings.allowMerging = false;
		settings.allowSplitting = true;
		settings.splittingDistanceCutoff = 20;
		settings.splittingTimeCutoff = 2;
		settings.splittingFeatureCutoffs.clear();
		System.out.println("Tracker settings:");
		System.out.println(settings.toString());
		inFileSettings.trackerSettings = settings;
		
		// 2 - Track the test spots
		LAPTracker lap;
		lap = new LAPTracker(spots, inFileSettings.trackerSettings);
		if (!lap.checkInput() || !lap.process())
			System.out.println(lap.getErrorMessage());

		// Print out track segments
		List<SortedSet<Spot>> trackSegments = lap.getTrackSegments();
		System.out.println("Found "+trackSegments.size()+" track segments:");
		for (SortedSet<Spot> trackSegment : trackSegments) {
			System.out.println("\n-*-*-*-*-* New Segment *-*-*-*-*-");
			for (Spot spot : trackSegment)
				System.out.println(Util.printCoordinates(spot.getPosition(null)) + ", Frame [" + spot.getFeature(Feature.POSITION_T) + "]");	
		}


		// 3 - Print out results for testing		
		System.out.println();
		System.out.println();
		System.out.println();
		SimpleGraph<Spot,DefaultEdge> graph = lap.getTrackGraph();
		ConnectivityInspector<Spot, DefaultEdge> inspector = new ConnectivityInspector<Spot, DefaultEdge>(graph);
		List<Set<Spot>> tracks = inspector.connectedSets();
		System.out.println("Found " + tracks.size() + " final tracks:");
		System.out.println();
		int counter = 0;
		for(Set<Spot> track : tracks) {
			System.out.println("Track "+counter);
			System.out.print("Spots in frames: \n");
			for(Spot spot : track)
				System.out.println(Util.printCoordinates(spot.getPosition(null)) + ", Frame [" + spot.getFeature(Feature.POSITION_T) + "]");
			System.out.println();
			counter++;
		}
		
		// 4 - Detailed info
//		System.out.println("Segment costs:");
//		LAPUtils.echoMatrix(lap.getSegmentCosts());
		
//		System.out.println();
//		System.out.println("Fragment costs for 1st frame:");
//		LAPUtils.echoMatrix(lap.getLinkingCosts().get(0));
		
		// 5 - Display tracks
		// Load Image
		ij.ImageJ.main(args);
		ImagePlus imp = null;
		imp = reader.getImage();
		if (imp != null)
			inFileSettings.imp = imp;
		
		SpotDisplayer sd2d = new HyperStackDisplayer(inFileSettings);
		sd2d.setSpots(spots);
		sd2d.render();
		sd2d.setSpotsToShow(spots);
		sd2d.setTrackGraph(graph);
		sd2d.setDisplayTrackMode(SpotDisplayer.TrackDisplayMode.ALL_WHOLE_TRACKS, 1);
	}

}
