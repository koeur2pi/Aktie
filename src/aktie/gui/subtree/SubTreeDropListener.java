package aktie.gui.subtree;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

public class SubTreeDropListener extends ViewerDropAdapter
{

    private SubTreeModel model;

    public SubTreeDropListener ( Viewer viewer, SubTreeModel mod )
    {
        super ( viewer );
        model = mod;
    }

    @Override
    public boolean performDrop ( Object d )
    {
        int loc = this.getCurrentLocation();
        Object tar = this.getCurrentTarget();
        System.out.println ( "Drop it like it's hot.. loc: " + loc + " obj: " + tar + " d.cls: " + d.getClass().getName() + " is " + d );

        if ( tar != null && tar instanceof SubTreeEntity )
        {
            model.dropped ( d, tar, getCurrentLocation() );
            getViewer().setInput ( "Here's some dropped data man" );
            return true;
        }

        return false;
    }

    @Override
    public boolean validateDrop ( Object arg0, int arg1, TransferData arg2 )
    {
        return true;
    }

}
