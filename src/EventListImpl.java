// DV Simulator

import java.util.Vector;

public class EventListImpl implements EventList
{
    private Vector<Event> data;
    
    public EventListImpl()
    {
        data = new Vector<>();
    }
    
    public boolean add(Event e)
    {
        data.addElement(e);
        return true;
    }
    
    public Event removeNext()
    {
        if (data.isEmpty())
        {
            return null;
        }
    
        int firstIndex = 0;
        double first = (data.elementAt(firstIndex)).getTime();
        for (int i = 0; i < data.size(); i++)
        {
            if ((data.elementAt(i)).getTime() < first)
            {
                first = (data.elementAt(i)).getTime();
                firstIndex = i;
            }
        }
        
        Event next = data.elementAt(firstIndex);
        data.removeElement(next);
    
        return next;
    }
    
    public String toString()
    {
        return data.toString();
    }
    
    public double getLastPacketTime(int entityFrom, int entityTo)
    {
        double time = 0.0;
        for (int i = 0; i < data.size(); i++)
        {
            if ((((data.elementAt(i))).getEntity() == entityTo) &&
                (((data.elementAt(i))).getPacket().getSource() ==
                                           entityFrom)
               )
            {
                time = ((data.elementAt(i))).getTime();
            }
        }
    
        return time;
    }
}
