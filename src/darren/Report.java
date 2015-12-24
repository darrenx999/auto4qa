package darren;

import java.awt.Color;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;


public class Report {

	static ArrayList<File> filelist = new ArrayList<File>();
	/**
	 * @param args
	 * Author:Darren Xu
	 */
	public static void main( String[] args )
	{
		String path = null; // The first argument
		
		if( args[0] != null )
		{
			path = args[0];
		}   else
		{
			System.err.println("You need to specify file name.");
		}
		
		//Start to look for files to analyze
		File f=new File(path);
		if(f.isDirectory())
		{
			System.out.println(f.getName()+" is a directory.");
			for(File s:f.listFiles())
			{
				filelist.add(s);
			}
		} else
		{
			System.out.println(f.getName()+" is a file.");
			filelist.add(f);
		}


		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		System.out.println(sdf.format(new Date())+" Start to handle the log ... ");
		Long startTime=System.currentTimeMillis();

		for(File file:filelist)
		{
			 Map<String,Map<String,Double>> apiHandleTimeSeries= new LinkedHashMap<String,Map<String,Double>>();
			 Map<String, String> status = new HashMap<String,String>();
						
			 ArrayList<Map> maplist=new ArrayList<Map>();
			 
			System.out.println(sdf.format(new Date())+" Start to handle the log "+ file);
			 try {
				maplist=logParse(file.toString());
				apiHandleTimeSeries=maplist.get(0);
				status=maplist.get(1);
				
				File root=new File("report");
				 if(!root.exists())
				 {
					root.mkdir();
				 }
				 
				File dir=new File(root+"/"+file.getName().split("\\.")[0]);
				if(!dir.exists())
				{
					dir.mkdir();
				}
				File f1=new File(dir+"/TimeSeries");
				if(!f1.exists())
				{
					f1.mkdir();
				}
				File f2=new File(dir+"/AccessTimes");
				if(!f2.exists())
				{
					f2.mkdir();
				}
				
				for(Iterator<?> it = apiHandleTimeSeries.keySet().iterator(); it.hasNext();)
				{
					Object key=it.next();
					Map<String, Double> response=apiHandleTimeSeries.get(key);
					JFreeChart timeSeriesChart = createTimeSeriesChart(key.toString(),createTimeSeriesDataset(response));
					ChartUtilities.saveChartAsPNG(new File(f1+"/"+key.toString()+".png"),timeSeriesChart,800,600);				
				}
				
				System.out.println(sdf.format(new Date())+"Start to handle CategoryChart.");
	
				JFreeChart categoryChart1 = createCategoryChart("Access Times",createCategoryDataset(status)[0]);
				ChartUtilities.saveChartAsPNG(new File(f2+"/statusAccessTimes.png"),categoryChart1,800,3600);
				JFreeChart categoryChart2 = createCategoryChart("Average Response",createCategoryDataset(status)[1]);
				ChartUtilities.saveChartAsPNG(new File(f2+"/statusAverageResponse.png"),categoryChart2,800,3600);		
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
		long endTime=System.currentTimeMillis();
		long duration=endTime-startTime;
		
		System.out.println("All processes spent "+duration+" ms.");
	}
	
	private static ArrayList<Map> logParse(String filename) throws IOException
	{
		 Map<String,Map<String,Double>> apiHandleTimeSeries= new LinkedHashMap<String,Map<String,Double>>();
		 Map<String, String> status = new HashMap<String,String>();
		

		 ArrayList<Map> mapList=new ArrayList<Map>();
		 
		long slogparse=System.currentTimeMillis();
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String str="";		
		String regex=PropertyManager.getStringProperty("report.regex", "");
		while ((str = in.readLine()) != null)
		{
			
			Pattern p=Pattern.compile(regex);
			Matcher m=p.matcher(str);
			if(m.find())
			{
				String time=m.group(1);
				Double handletime=Double.parseDouble(m.group(3));
				String api=m.group(2);
			
				
				if(apiHandleTimeSeries.containsKey(api))
				{
					apiHandleTimeSeries.get(api).put(time, handletime);
					String[] tr=status.get(api).split(":");
					int inTimes=Integer.parseInt(tr[0])+1;
					double inHandleTime=Double.parseDouble(tr[1]);
					status.remove(api);
					status.put(api, inTimes+":"+(inHandleTime+handletime));
				} else
				{
					Map<String,Double> tmpMap=new HashMap<String,Double>();
					tmpMap.put(time, handletime);
					apiHandleTimeSeries.put(api,tmpMap);
					status.put(api,1+":"+handletime);
				}				
			}		
		}
		in.close();
		mapList.add(apiHandleTimeSeries);
		mapList.add(status);
		long elogparse=System.currentTimeMillis();
		long d=elogparse-slogparse;
		System.out.println("Log parse acion spent "+d+" ms.");
		
		return mapList;
	}
	
	private static JFreeChart createTimeSeriesChart(String name,XYDataset dataset) {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            name,  // title
            "Date",         // x-axis label
            "Reponse Time - ms",   // y-axis label
            dataset,            // data
            true,               // create legend?
            false,               // generate tooltips?
            false               // generate URLs?
        );

        
        chart.setBackgroundPaint(Color.white);   
        XYPlot plot = (XYPlot) chart.getPlot();

		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        NumberAxis numberaxis = (NumberAxis)plot.getRangeAxis();
        numberaxis.setNumberFormatOverride(new DecimalFormat("0.000")); 
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
        }           
        return chart;

    }
	
	private static XYDataset createTimeSeriesDataset(Map<String,Double> map) {
		XYSeries s1 = new XYSeries("");
		for(Iterator<?> it = map.keySet().iterator(); it.hasNext();)
        {
        	Object key=it.next();
        	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        	try {
				s1.add(sdf.parse(key.toString()).getTime(),map.get(key));
			} catch (ParseException e) {
				e.printStackTrace();
			}
        }
        XYSeriesCollection currentDataset = new XYSeriesCollection();		
        currentDataset.addSeries(s1);     
        return currentDataset;
    }
	
	public static JFreeChart createCategoryChart(String name,CategoryDataset categoryDataset) {  
        JFreeChart jfreechart = ChartFactory.createBarChart(
        		name,   
                "API name",  
                "",  
                categoryDataset,  
                PlotOrientation.HORIZONTAL, 
                true,  
                false,  
                false);  
   
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println(sdf.format(new Date())+"Start to create CategoryChart.");
        jfreechart.setTextAntiAlias(false);  
        jfreechart.setBackgroundPaint(Color.white);  
  
        CategoryPlot plot = jfreechart.getCategoryPlot();
        plot.setRangeGridlinesVisible(true);  
        plot.setRangeGridlinePaint(Color.blue);  
        //plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        //NumberAxis vn = (NumberAxis) plot.getRangeAxis();  
        //vn.setAutoRangeIncludesZero(true);  
        //DecimalFormat df = new DecimalFormat("#0.0");  
        //vn.setNumberFormatOverride(df); 
  
        CategoryAxis domainAxis = plot.getDomainAxis(); 
        domainAxis.setMaximumCategoryLabelWidthRatio(6.00f);
        domainAxis.setLowerMargin(0.0);  
        domainAxis.setUpperMargin(0.0);   
      
        plot.setDomainAxis(domainAxis);    
        plot.setBackgroundPaint(new Color(255, 255, 204));  
        
        ValueAxis rangeAxis = plot.getRangeAxis();   
        rangeAxis.setUpperMargin(0.5);  
        rangeAxis.setLowerMargin(0.5);

        plot.setRangeAxis(rangeAxis);  
  
        BarRenderer renderer = new BarRenderer();    
        renderer.setMaximumBarWidth(0.1);          
        renderer.setMinimumBarLength(0.1);           
        renderer.setBaseOutlinePaint(Color.BLACK);          
        renderer.setDrawBarOutline(true);    
        renderer.setSeriesPaint(0, Color.decode("#B22222"));  
        renderer.setSeriesPaint(1, Color.decode("#1C86EE")); 
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_RIGHT));
        renderer.setItemLabelAnchorOffset(40.0f);
        renderer.setItemMargin(1);  
        jfreechart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);  
          
        renderer.setIncludeBaseInRange(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());  
        renderer.setBaseItemLabelsVisible(true);  
               
        plot.setRenderer(renderer);  
        plot.setForegroundAlpha(1.0f);  
        plot.setBackgroundAlpha(0.5f);  
        return jfreechart;  
    }  
	
	 public static CategoryDataset[] createCategoryDataset(Map<String, String> status) { 
		 	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		 	System.out.println(sdf.format(new Date())+"Start to handle CategoryDataet.");		 	
		 	List list1 = new ArrayList();
		 	List list2 = new ArrayList();
		 	for(Iterator<?> it = status.keySet().iterator(); it.hasNext();)
	        {
		 		Object key=it.next();
		 		String[] value=status.get(key).split(":");
		 		double times=Double.parseDouble(value[0]);
		 		list1.add(new ThreeData(times,"Access Times",key.toString()));
		 		list2.add(new ThreeData(Double.parseDouble(value[1])/times,"Average Handling time",key.toString()));
	        }
		 	Collections.sort(list1,new ThreeData());
		 	Collections.sort(list2,new ThreeData());

		 	DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
		 	DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
	        Iterator iter1 = list1.iterator();
	        while(iter1.hasNext()) {
	        	ThreeData threeDataTmp = (ThreeData)iter1.next();
	        	dataset1.addValue(threeDataTmp.value, threeDataTmp.rowKey, threeDataTmp.colKey);
	        }
	        Iterator iter2 = list2.iterator();
	        while(iter2.hasNext()) {
	        	ThreeData threeDataTmp = (ThreeData)iter2.next();
	        	dataset2.addValue(threeDataTmp.value, threeDataTmp.rowKey, threeDataTmp.colKey);
	        }
	        
	        CategoryDataset[] dataset = {dataset1,dataset2};
	        return dataset;  
	    }
}

