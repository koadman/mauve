package org.gel.mauve.gui.sequence;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.gui.MauveRenderingHints;
import org.gel.mauve.histogram.ZoomHistogram;

public class HistogramPanel extends AbstractSequencePanel {
	ZoomHistogram hist;
	BasicStroke meanStroke = new BasicStroke(2);

	HistogramPanel(BaseViewerModel model, Genome genome, ZoomHistogram zh){
		super(model, genome);
		hist = zh;
		setOpaque (false);
		setDoubleBuffered(true);
	};	
	
	/**
	 * paint a pre-computed histogram
	 */
	public void paintComponent (Graphics graphics) {
		if(!model.getDrawAttributes()) return;
		Graphics2D g2 = (Graphics2D)graphics;
        Double density = (Double) g2.getRenderingHint(MauveRenderingHints.KEY_SIMILARITY_DENSITY);
        double increment = density == null ? 1.0 : density.doubleValue();
        double sim_height = this.getHeight();
        double prevh = sim_height;
        double half_inc = increment/2.0;
        Stroke curStroke = g2.getStroke();
        g2.setStroke(meanStroke);
        for (double pixelD = 0; pixelD < getWidth(); pixelD += increment)
        {
            long seq_left = pixelToCenterSequenceCoordinate(pixelD);
            long seq_right = pixelToCenterSequenceCoordinate(pixelD + increment);
            seq_left = seq_left < 0 ? 0 : seq_left;	// clamp to valid range
            seq_right = seq_right < getGenome().getLength() ? seq_right : getGenome().getLength();	// clamp to valid range
            if(seq_right <= 0)	continue;
            double s = hist.getValueForRange(seq_left, seq_right);
            double smin = hist.getValueForRange(seq_left, seq_right,-1);
            double smax = hist.getValueForRange(seq_left, seq_right,1);
            // normalize to a box_height
            double height = sim_height - (((double) s + 127d) / 256d * sim_height);
            double heightmin = sim_height - (((double) smin + 127d) / 256d * sim_height);
            double heightmax = sim_height - (((double) smax + 127d) / 256d * sim_height);

            double x1=pixelD-half_inc;
            double x2=pixelD+half_inc;
            Rectangle2D.Double r2d = new Rectangle2D.Double(x1, heightmax, x2-x1, heightmin-heightmax);
            g2.setColor(Color.yellow);
            g2.fill(r2d);
            g2.setColor(Color.black);
            Line2D.Double l2d = new Line2D.Double(x1, prevh, x2, height);
            g2.draw(l2d);
            prevh = height;
        }
        g2.setStroke(curStroke);
	}
}
