package darren;

import java.util.Comparator;

public class ThreeData implements Comparator{
	public String rowKey;
	public String colKey;
	public double value;

	public ThreeData(){}

	public ThreeData(double value, String rowKey, String colKey){
		this.value = value;
		this.rowKey = rowKey;
		this.colKey = colKey;
	}

	public int compare(Object obj1, Object obj2) {
		double valueTmp = ((ThreeData)obj1).value - ((ThreeData)obj2).value;
		return valueTmp > 0 ? -1 : (valueTmp < 0 ? 1 : 0); 
	}

}
