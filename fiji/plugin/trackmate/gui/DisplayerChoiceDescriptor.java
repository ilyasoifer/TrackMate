package fiji.plugin.trackmate.gui;

import java.awt.Component;

import fiji.plugin.trackmate.TrackMate_;
import fiji.plugin.trackmate.segmentation.ManualSegmenter;
import fiji.plugin.trackmate.visualization.TrackMateModelView;

public class DisplayerChoiceDescriptor implements WizardPanelDescriptor {

	public static final String DESCRIPTOR = "DisplayerChoice";
	private TrackMate_ plugin;
	private ListChooserPanel<TrackMateModelView> component;
	private TrackMateWizard wizard;
	
	/*
	 * METHODS
	 */
	
	@Override
	public void setWizard(TrackMateWizard wizard) {
		this.wizard = wizard;
	}

	@Override
	public Component getPanelComponent() {
		return component;
	}

	@Override
	public String getThisPanelID() {
		return DESCRIPTOR;
	}

	@Override
	public String getNextPanelID() {
		return LaunchDisplayerDescriptor.DESCRIPTOR;
	}

	@Override
	public String getPreviousPanelID() {
		if (plugin.getModel().getSettings().segmenter.getClass() == ManualSegmenter.class) {
			return SegmenterConfigurationPanelDescriptor.DESCRIPTOR;
		} else {
			return InitFilterPanel.DESCRIPTOR;
		}
	}

	@Override
	public void aboutToDisplayPanel() {	}

	@Override
	public void displayingPanel() { }

	@Override
	public void aboutToHidePanel() {
		TrackMateModelView displayer = component.getChoice();
		wizard.setDisplayer(displayer);
	}

	@Override
	public void setPlugin(TrackMate_ plugin) {
		this.plugin = plugin;
		this.component = new ListChooserPanel<TrackMateModelView>(plugin.getAvailableTrackMateModelViews(), "displayer");
	}



}
