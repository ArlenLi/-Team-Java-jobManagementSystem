package GUI;

public class Starter {

	private String[][] data = {};
	
	public void addRow(String a,String b,String c)
	{
		int newCol = data.length+1;
		String[][] temp = new String[newCol][3];
		
		for(int j=0;j<data.length;j++)
		{
			for(int i=0;i<3;i++)
			{
				temp[j][i] = data[j][i];
			}
		}		
		
		temp[newCol-1][0] = a;
		temp[newCol-1][1] = b;
		temp[newCol-1][2] = c;
		data = temp;
	}
	
	public void printData()
	{
		System.out.println("");
		for(int j=0;j<data.length;j++)
		{
			for(int i=0;i<3;i++)
			{
				System.out.print(data[j][i]+" ");
			}
			System.out.println();
		}	
	}
	
	public static void main(String[] args)
	{
		/*
		Starter s = new Starter();
		s.addRow("1","1","1");
		s.printData();
		s.addRow("2", "d", "2");
		s.printData();
		s.addRow("3", "3", "3");
		s.printData();
		*/
		new GUInterface();
	}

}
