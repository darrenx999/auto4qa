package darren;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Properties;

public class PropertyManager extends Thread
{
	private long										m_lLastUpdated	= 0;
	private final static String							s_sFileName		= "config.properties";						// config
	private static Properties							s_Properties	= new Properties();

	private static PropertyManager						s_propmanager	= null;

	public static boolean run = true;
	static
	{
		System.out.println("Init PropertyManager!");
		_getInstance(); // Here to ensure the property manager instance is created
	}
	

	/**
	 * The PropertyManager Class is a singleton.
	 */
	public PropertyManager()
	{
		setName("PropertyManager");
		if( !new File(s_sFileName).exists() )
		{
			System.out.println("FATAL ERROR: '" + s_sFileName + "' not found in current working directory.");
		}
		
		loadProperties();
	}

	private static PropertyManager _getInstance()
	{
		if( null == s_propmanager )
		{
			s_propmanager = new PropertyManager();
			s_propmanager.start();
		}

		return s_propmanager;
	}
	
	public interface PropertyListener extends EventListener
	{
		public void propertyChanged( String sKey, String sValue );
	}

	public static Properties getPropertys()
	{
		return _getInstance().s_Properties;
	}

	public static String getProperty( String sKey )
	{
		return s_Properties.getProperty(sKey);
	}
	

	public boolean loadProperties()
	{
		boolean bRet = false;

		File sF = new File(s_sFileName);
		if( !sF.exists() )
		{
			System.exit(-1);
		}

		if( sF.exists() && sF.lastModified() != m_lLastUpdated )
		{
			bRet = true;
			m_lLastUpdated = sF.lastModified();

			Properties newProperties = new Properties();
			FileInputStream fis = null;

			try
			{
				fis = new FileInputStream(sF);
				newProperties.load(fis);

				for( Iterator<?> it = newProperties.keySet().iterator(); it.hasNext(); )
				{
					String sNewProp = ((String) it.next());
					String sNewVal = (String) newProperties.get(sNewProp);
					if( !s_Properties.containsKey(sNewProp) )
					{
						s_Properties.put(sNewProp, sNewVal);
					}
					else if( !s_Properties.get(sNewProp).equals(sNewVal) )
					{
						s_Properties.put(sNewProp, sNewVal);
					}
				}

			}
			catch( FileNotFoundException ex )
			{
				ex.printStackTrace();
			}
			catch( IOException ex )
			{
				ex.printStackTrace();
			}
			finally
			{
				if( fis != null )
				{
					try
					{
						fis.close();
					}
					catch( Exception ex )
					{
						ex.printStackTrace();
					}
				}
			}
		}
		return bRet;
	}

	
	public static boolean getBooleanProperty( String sProperty, boolean fDefault )
	{
		return _getInstance()._getBooleanProperty(sProperty, fDefault);
	}

	private boolean _getBooleanProperty( String sProperty, boolean fDefault )
	{
		Boolean b = Boolean.parseBoolean(getProperty(sProperty));
		if( null == b )
			return false;
		return b;
	}

	
	public static int getIntegerProperty( String sProperty, int iDefault )
	{
		return _getInstance()._getIntegerProperty(sProperty, iDefault);
	}

	private int _getIntegerProperty( String sProperty, int iDefault )
	{
		Integer i = null;
		try
		{
			i = Integer.parseInt(getProperty(sProperty));
		}
		catch( NumberFormatException ex )
		{
			ex.printStackTrace();
		}
		if( null == i )
			return iDefault;
		return i;
	}

	
	public static double getDoubleProperty( String sProperty, double dDefault )
	{
		return _getInstance()._getDoubleProperty(sProperty, dDefault);
	}

	private double _getDoubleProperty( String sProperty, double dDefault )
	{
		Double d = null;
		try
		{
			d = Double.parseDouble(getProperty(sProperty));
		}
		catch( NumberFormatException ex )
		{
			ex.printStackTrace();
		}
		catch( NullPointerException ex )
		{
			ex.printStackTrace();
		}
		if( null == d )
			return dDefault;
		return d;
	}

	
	public static long getLongProperty( String sProperty, long lDefault )
	{
		return _getInstance()._getLongProperty(sProperty, lDefault);
	}

	private long _getLongProperty( String sProperty, long lDefault )
	{
		Long l = null;
		try
		{
			l = Long.parseLong(getProperty(sProperty));
		}
		catch( NumberFormatException ex )
		{
			ex.printStackTrace();
		}
		catch( NullPointerException ex )
		{
			ex.printStackTrace();
		}

		if( null == l )
			return lDefault;
		return l;
	}

	
	public static String getStringProperty( String sProperty, String sDefault )
	{
		return _getInstance()._getStringProperty(sProperty, sDefault);
	}

	private String _getStringProperty( String sProperty, String sDefault )
	{
		String s = getProperty(sProperty);
		if( null == s )
			return sDefault;
		return s;
	}
}

