package aktie.net;

import java.util.Set;

import aktie.data.RequestFile;

public interface GetSendData2
{

    public Object nextNonFile ( String localdest, String remotedest, Set<String> members, Set<String> subs );

    public Object nextFile ( String localdest, String remotedest, Set<RequestFile> hasfiles );

    public Set<RequestFile> getHasFileForConnection ( String remotedest, Set<String> subs );

    public long getLastFileUpdate();

}
