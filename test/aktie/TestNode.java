package aktie;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import aktie.crypto.Utils;
import aktie.data.CObj;
import aktie.data.DirectoryShare;
import aktie.gui.GuiCallback;
import aktie.gui.Wrapper;
import aktie.index.CObjList;
import aktie.net.ConnectionListener;
import aktie.net.ConnectionManager2;
import aktie.net.ConnectionThread;
import aktie.net.RawNet;
import aktie.utils.FUtils;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortedNumericSortField;
import org.junit.Test;

public class TestNode
{

    public class CallbackIntr implements GuiCallback
    {

        public ConcurrentLinkedQueue<Object> oqueue = new ConcurrentLinkedQueue<Object>();

        @Override

        public synchronized void update ( Object o )
        {
            if ( o instanceof CObj )
            {
                CObj co = ( CObj ) o;

                if ( co.getType() != null && co.getString ( CObj.ERROR ) == null )
                {
                    oqueue.add ( o );
                }

            }

            else
            {
                oqueue.add ( o );
            }

            notifyAll();
        }

        public synchronized void waitForUpdate()
        {
            int trys = 60;

            while ( oqueue.size() == 0 && trys > 0 )
            {
                trys--;

                try
                {
                    wait ( 1000 );
                }

                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                }

            }

        }

    }

    public class ConCallbackIntr implements ConnectionListener
    {

        public ConcurrentLinkedQueue<ConnectionThread> oqueue = new ConcurrentLinkedQueue<ConnectionThread>();

        @Override
        public void update ( ConnectionThread ct )
        {
            oqueue.add ( ct );
        }

        @Override
        public void closed ( ConnectionThread ct )
        {
            oqueue.add ( ct );
        }

        @Override
        public void bytesReceived ( long bytes )
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void bytesSent ( long bytes )
        {
            // TODO Auto-generated method stub

        }

    }

    private CObj createIdentity ( Node n, String name )
    {
        CObj nn = new CObj();
        nn.setType ( CObj.IDENTITY );
        nn.pushString ( CObj.NAME, name );
        n.enqueue ( nn );
        n.sendRequestsNow();
        return nn;
    }

    private void printValidMember ( CObjList clst )
    {
        if ( clst.size() > 0 )
        {
            try
            {
                CObj co = clst.get ( 0 );
                System.out.println ( " Valid? " + co.getPrivate ( CObj.VALIDMEMBER ) +
                                     " decoded: " + co.getPrivate ( CObj.DECODED ) +
                                     " lastup: " + co.getPrivateNumber ( CObj.LASTUPDATE ) );
            }

            catch ( Exception e )
            {
                e.printStackTrace();
            }

        }

    }

    @Test
    public void testNode()
    {
        Wrapper.OLDPAYMENT = 0;
        Wrapper.NEWPAYMENT = 0x0400004000000000L;

        Logger log = Logger.getLogger ( "aktie" );
        log.setLevel ( Level.INFO );

        ConnectionManager2.MIN_TIME_TO_NEW_CONNECTION = 2L * 1000L;
        ConnectionManager2.DECODE_AND_NEW_CONNECTION_DELAY = 1000L;
        ConnectionManager2.REQUEST_UPDATE_DELAY = 200L;

        CallbackIntr cb0 = new CallbackIntr();
        CallbackIntr cb1 = new CallbackIntr();
        CallbackIntr cb2 = new CallbackIntr();
        CallbackIntr cb3 = new CallbackIntr();
        CallbackIntr cb4 = new CallbackIntr();

        ConCallbackIntr cn0 = new ConCallbackIntr();
        ConCallbackIntr cn1 = new ConCallbackIntr();
        ConCallbackIntr cn2 = new ConCallbackIntr();
        ConCallbackIntr cn3 = new ConCallbackIntr();
        ConCallbackIntr cn4 = new ConCallbackIntr();

        File tmpdir = new File ( "tstnode" );
        FUtils.deleteDir ( tmpdir );
        tmpdir.mkdirs();

        RawNet net0 = new RawNet ( tmpdir );
        RawNet net1 = new RawNet ( tmpdir );
        RawNet net2 = new RawNet ( tmpdir );
        RawNet net3 = new RawNet ( tmpdir );
        RawNet net4 = new RawNet ( tmpdir );

        //public Node(String nodedir, Net net, GuiCallback uc,
        //      GuiCallback nc, ConnectionListener cc) throws IOException {

        try
        {
            FUtils.deleteDir ( new File ( "testnode0" ) );
            FUtils.deleteDir ( new File ( "testnode1" ) );
            FUtils.deleteDir ( new File ( "testnode2" ) );
            FUtils.deleteDir ( new File ( "testnode3" ) );
            FUtils.deleteDir ( new File ( "testnode4" ) );

            Node n0 = new Node ( "testnode0", net0, cb0, cb0, cn0 );
            Node n1 = new Node ( "testnode1", net1, cb1, cb1, cn1 );
            Node n2 = new Node ( "testnode2", net2, cb2, cb2, cn2 );
            Node n3 = new Node ( "testnode3", net3, cb3, cb3, cn3 );
            Node n4 = new Node ( "testnode4", net4, cb4, cb4, cn4 );

            System.out.println ( "CREATE IDENTIES.............................." );
            //CObj node0a =
            createIdentity ( n0, "node0a" );
            //CObj node1a =
            createIdentity ( n1, "node1a" );
            CObj node2a = createIdentity ( n2, "node2a" );
            //CObj node3a =
            createIdentity ( n3, "node3a" );

            cb0.waitForUpdate();
            Object n0dat = cb0.oqueue.poll();
            assertEquals ( "node0a", ( ( CObj ) n0dat ).getString ( CObj.NAME ) );

            cb1.waitForUpdate();
            Object n1dat = cb1.oqueue.poll();
            assertEquals ( "node1a", ( ( CObj ) n1dat ).getString ( CObj.NAME ) );

            cb2.waitForUpdate();
            Object n2dat = cb2.oqueue.poll();
            assertEquals ( "node2a", ( ( CObj ) n2dat ).getString ( CObj.NAME ) );

            cb3.waitForUpdate();
            Object n3dat = cb3.oqueue.poll();
            assertEquals ( "node3a", ( ( CObj ) n3dat ).getString ( CObj.NAME ) );

            //CObj node0b =
            createIdentity ( n0, "node0b" );
            //CObj node1b =
            createIdentity ( n1, "node1b" );
            //CObj node2b =
            createIdentity ( n2, "node2b" );
            CObj node3b = createIdentity ( n3, "node3b" );

            cb0.waitForUpdate();
            n0dat = cb0.oqueue.poll();
            assertEquals ( "node0b", ( ( CObj ) n0dat ).getString ( CObj.NAME ) );

            cb1.waitForUpdate();
            n1dat = cb1.oqueue.poll();
            assertEquals ( "node1b", ( ( CObj ) n1dat ).getString ( CObj.NAME ) );

            cb2.waitForUpdate();
            n2dat = cb2.oqueue.poll();
            assertEquals ( "node2b", ( ( CObj ) n2dat ).getString ( CObj.NAME ) );

            cb3.waitForUpdate();
            n3dat = cb3.oqueue.poll();
            assertEquals ( "node3b", ( ( CObj ) n3dat ).getString ( CObj.NAME ) );

            n0.getIndex().forceNewSearcher();
            CObjList clist = n0.getIndex().getMyIdentities();

            for ( int c = 0; c < clist.size(); c++ )
            {
                CObj co = clist.get ( c );
                System.out.println ( "Nm: " + co.getString ( CObj.NAME ) );
            }

            assertEquals ( 2, clist.size() );
            CObj n0seed = clist.get ( 0 );
            CObj n0seedb = clist.get ( 1 );
            clist.close();

            System.out.println ( "SEED NODES.............................." );
            n0seed.getPrivatedata().clear();  //*** OTHERWISE MINE IS SET TO TRUE!!!!!!!
            //n0seed.getPrivateNumbers().clear();

            cb1.oqueue.clear();
            n0seed.setType ( CObj.USR_SEED );
            n1.enqueue ( n0seed );

            n1.sendRequestsNow();

            cb1.waitForUpdate();

            try
            {
                Thread.sleep ( 500 );
            }

            catch ( InterruptedException e1 )
            {
                e1.printStackTrace();
            }

            cb2.oqueue.clear();
            n0seed.setType ( CObj.USR_SEED );
            n2.enqueue ( n0seed );

            n2.sendRequestsNow();

            cb2.waitForUpdate();

            try
            {
                Thread.sleep ( 500 );
            }

            catch ( InterruptedException e1 )
            {
                e1.printStackTrace();
            }

            cb3.oqueue.clear();
            n0seed.setType ( CObj.USR_SEED );
            n3.enqueue ( n0seed );

            n3.sendRequestsNow();

            cb3.waitForUpdate();

            try
            {
                Thread.sleep ( 500 );
            }

            catch ( InterruptedException e1 )
            {
                e1.printStackTrace();
            }


            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            System.out.println ( "UPDATE NODES.............................." );
            CObj updateIdent = new CObj();
            updateIdent.setType ( CObj.USR_IDENTITY_UPDATE );

            n1.enqueue ( updateIdent );
            n2.enqueue ( updateIdent );
            n3.enqueue ( updateIdent );
            n0.enqueue ( updateIdent );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();
            n0.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n0.enqueue ( updateIdent );

            n0.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            CObjList clst = n0.getIndex().getIdentities();
            assertEquals ( 8, clst.size() );
            clst.close();

            n1.enqueue ( updateIdent );
            n2.enqueue ( updateIdent );
            n3.enqueue ( updateIdent );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.enqueue ( updateIdent );
            n1.enqueue ( updateIdent );
            n2.enqueue ( updateIdent );
            n3.enqueue ( updateIdent );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 12L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            clist = n0.getIndex().getIdentities();
            System.out.println ( "NUM NODES KNOWN0: " + clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                CObj nid = clist.get ( c );
                System.out.println ( "Name: " + nid.getString ( CObj.NAME ) );
            }

            assertEquals ( 8, clist.size() );
            clist.close();

            clist = n1.getIndex().getIdentities();
            System.out.println ( "NUM NODES KNOWN1: " + clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                CObj nid = clist.get ( c );
                System.out.println ( "Name: " + nid.getString ( CObj.NAME ) );
            }

            assertEquals ( 8, clist.size() );
            clist.close();

            clist = n2.getIndex().getIdentities();
            System.out.println ( "NUM NODES KNOWN2: " + clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                CObj nid = clist.get ( c );
                System.out.println ( "Name: " + nid.getString ( CObj.NAME ) );
            }

            assertEquals ( 8, clist.size() );
            clist.close();

            clist = n3.getIndex().getIdentities();
            System.out.println ( "NUM NODES KNOWN3: " + clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                CObj nid = clist.get ( c );
                System.out.println ( "Name: " + nid.getString ( CObj.NAME ) );
            }

            assertEquals ( 8, clist.size() );
            clist.close();

            System.out.println ( "SET DEV ID...................................." );
            n0.newDeveloperIdentity ( n0seed.getId() );
            n1.newDeveloperIdentity ( n0seed.getId() );
            n2.newDeveloperIdentity ( n0seed.getId() );
            n3.newDeveloperIdentity ( n0seed.getId() );
            n4.newDeveloperIdentity ( n0seed.getId() );


            System.out.println ( "CREATE COMMUNITY.............................." );
            cb0.oqueue.clear();
            CObj com0n0 = new CObj();
            com0n0.setType ( CObj.COMMUNITY );
            com0n0.pushPrivate ( CObj.NAME, "com0n0" );
            com0n0.pushPrivate ( CObj.DESCRIPTION, "description com0n0" );
            com0n0.pushString ( CObj.CREATOR, n0seed.getId() );
            com0n0.pushString ( CObj.SCOPE, CObj.SCOPE_PRIVATE );
            n0.enqueue ( com0n0 );
            cb0.waitForUpdate();
            CObj co = ( CObj ) cb0.oqueue.poll();
            assertNull ( co.getString ( CObj.ERROR ) );

            System.out.println ( "UPDATE COMMUNITY.............................." );
            CObj comupdate = new CObj();
            comupdate.setType ( CObj.USR_COMMUNITY_UPDATE );
            n1.enqueue ( comupdate );
            n2.enqueue ( comupdate );
            n3.enqueue ( comupdate );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n1.enqueue ( comupdate );
            n2.enqueue ( comupdate );
            n3.enqueue ( comupdate );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            CObjList comlst = n0.getIndex().getValidCommunities();
            assertEquals ( 1, comlst.size() );
            comlst.close();

            comlst = n1.getIndex().getValidCommunities();
            assertEquals ( 0, comlst.size() );
            comlst.close();

            comlst = n2.getIndex().getValidCommunities();
            assertEquals ( 0, comlst.size() );
            comlst.close();

            comlst = n3.getIndex().getValidCommunities();
            assertEquals ( 0, comlst.size() );
            comlst.close();

            comlst = n1.getIndex().getCommunities ( n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, comlst.size() );
            System.out.println ( "N1 size: " + comlst.size() );
            comlst.close();

            comlst = n2.getIndex().getCommunities ( n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, comlst.size() );
            System.out.println ( "N2 size: " + comlst.size() );
            comlst.close();

            comlst = n3.getIndex().getCommunities ( n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, comlst.size() );
            System.out.println ( "N3 size: " + comlst.size() );
            comlst.close();

            System.out.println ( "CREATE MEMBERSHIP.............................." );
            CObj mem0 = new CObj();
            mem0.setType ( CObj.MEMBERSHIP );
            mem0.pushString ( CObj.CREATOR, n0seed.getId() );
            mem0.pushPrivate ( CObj.MEMBERID, node2a.getId() );
            mem0.pushPrivate ( CObj.COMMUNITYID, com0n0.getDig() );
            mem0.pushPrivateNumber ( CObj.AUTHORITY, CObj.MEMBER_CAN_GRANT );
            n0.enqueue ( mem0 );

            n0.sendRequestsNow();

            try
            {
                Thread.sleep ( 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            System.out.println ( "UPDATE MEMBERSHIP.............................." );
            CObj memupdate = new CObj();
            memupdate.setType ( CObj.USR_MEMBER_UPDATE );
            n1.enqueue ( memupdate );
            n2.enqueue ( memupdate );
            n3.enqueue ( memupdate );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n1.enqueue ( memupdate );
            n2.enqueue ( memupdate );
            n3.enqueue ( memupdate );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            clist = n1.getIndex().getMemberships ( n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            assertNull ( co.getPrivateNumber ( CObj.AUTHORITY ) );
            assertNull ( co.getPrivate ( CObj.COMMUNITYID ) );
            assertNull ( co.getPrivate ( CObj.MEMBERID ) );
            assertNull ( co.getPrivate ( CObj.KEY ) );
            System.out.println ( co.getString ( CObj.ENCKEY ) );
            System.out.println ( co.getString ( CObj.PAYLOAD ) );
            System.out.println ( co.getString ( CObj.PAYLOAD2 ) );
            clist.close();

            clist = n2.getIndex().getMemberships ( n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            System.out.println ( "CO: " + co + " co.getPrivateNumber: " + co.getPrivateNumber ( CObj.AUTHORITY ) );
            assertEquals ( ( long ) CObj.MEMBER_CAN_GRANT, ( long ) co.getPrivateNumber ( CObj.AUTHORITY ) );
            assertEquals ( com0n0.getDig(), co.getPrivate ( CObj.COMMUNITYID ) );
            assertEquals ( node2a.getId(), co.getPrivate ( CObj.MEMBERID ) );
            assertEquals ( "true", co.getPrivate ( CObj.VALIDMEMBER ) );
            assertNotNull ( co.getPrivate ( CObj.KEY ) );
            System.out.println ( co.getString ( CObj.ENCKEY ) );
            System.out.println ( co.getString ( CObj.PAYLOAD ) );
            System.out.println ( co.getString ( CObj.PAYLOAD2 ) );
            clist.close();

            clist = n3.getIndex().getMemberships ( n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            assertNull ( co.getPrivateNumber ( CObj.AUTHORITY ) );
            assertNull ( co.getPrivate ( CObj.COMMUNITYID ) );
            assertNull ( co.getPrivate ( CObj.MEMBERID ) );
            assertNull ( co.getPrivate ( CObj.KEY ) );
            System.out.println ( co.getString ( CObj.ENCKEY ) );
            System.out.println ( co.getString ( CObj.PAYLOAD ) );
            System.out.println ( co.getString ( CObj.PAYLOAD2 ) );
            clist.close();

            System.out.println ( "CREATE SUBSCRIPTION.............................." );
            cb0.oqueue.clear();
            CObj sub0 = new CObj();
            sub0.setType ( CObj.SUBSCRIPTION );
            sub0.pushString ( CObj.CREATOR, n0seed.getId() );
            sub0.pushString ( CObj.COMMUNITYID, com0n0.getDig() );
            sub0.pushString ( CObj.SUBSCRIBED, "true" );
            n0.enqueue ( sub0 );

            n0.sendRequestsNow();

            cb0.waitForUpdate();
            n0.getIndex().forceNewSearcher();
            co = n0.getIndex().getSubscription ( com0n0.getDig(), n0seed.getId() );
            assertNotNull ( co );
            assertEquals ( com0n0.getDig(), co.getString ( CObj.COMMUNITYID ) );
            assertEquals ( n0seed.getId(), co.getString ( CObj.CREATOR ) );
            assertEquals ( "true", co.getString ( CObj.SUBSCRIBED ) );

            System.out.println ( ".........................UPDATE SUBSCRIPTION.............................." );
            CObj updatesubs = new CObj();
            updatesubs.setType ( CObj.USR_SUB_UPDATE );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n0.closeAllConnections();
            n1.closeAllConnections();
            n2.closeAllConnections();
            n3.closeAllConnections();

            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            clist = n1.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 0, clist.size() );
            clist.close();

            n2.getIndex().forceNewSearcher();
            clist = n2.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            assertEquals ( n0seed.getId(), co.getString ( CObj.CREATOR ) );
            assertEquals ( com0n0.getDig(), co.getString ( CObj.COMMUNITYID ) );
            clist.close();

            n3.getIndex().forceNewSearcher();
            clist = n3.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 0, clist.size() );
            clist.close();

            System.out.println ( "CREATE MEMBERSHIP.............................." );
            clist = n0.getIndex().getMemberships ( node2a.getId(), 0, 99999 );
            System.out.println ( "1 N0 has n2 membership grant: " + clist.size() );
            clist.close();
            clist = n1.getIndex().getMemberships ( node2a.getId(), 0, 99999 );
            System.out.println ( "1 N1 has n2 membership grant: " + clist.size() );
            clist.close();
            clist = n2.getIndex().getMemberships ( node2a.getId(), 0, 99999 );
            System.out.println ( "1 N2 has n2 membership grant: " + clist.size() );
            clist.close();
            clist = n3.getIndex().getMemberships ( node2a.getId(), 0, 99999 );
            System.out.println ( "1 N3 has n2 membership grant: " + clist.size() );
            clist.close();

            cb2.oqueue.clear();
            CObj mem2 = new CObj();
            mem2.setType ( CObj.MEMBERSHIP );
            mem2.pushString ( CObj.CREATOR, node2a.getId() );
            mem2.pushPrivate ( CObj.MEMBERID, node3b.getId() );
            mem2.pushPrivate ( CObj.COMMUNITYID, com0n0.getDig() );
            mem2.pushPrivateNumber ( CObj.AUTHORITY, CObj.MEMBER_SIMPLE );
            n2.enqueue ( mem2 );

            n2.sendRequestsNow();

            cb2.waitForUpdate();
            n2.getIndex().forceNewSearcher();

            clist = n2.getIndex().getMemberships ( com0n0.getDig(), null );
            System.out.println ( "N2*: " + clist.size() );
            assertEquals ( 2, clist.size() );
            clist.close();

            Object o = cb2.oqueue.poll();
            co = ( CObj ) o;
            assertNull ( co.getString ( CObj.ERROR ) );


            try
            {
                Thread.sleep ( 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            System.out.println ( "UPDATE MEMBERSHIP.............................." );

            for ( int c = 0; c < 4; c++ )
            {
                memupdate = new CObj();
                memupdate.setType ( CObj.USR_MEMBER_UPDATE );

                if ( c % 2 == 0 )
                {
                    n0.enqueue ( memupdate );
                    n1.enqueue ( memupdate );
                    n2.enqueue ( memupdate );
                    n3.enqueue ( memupdate );

                    n0.sendRequestsNow();
                    n1.sendRequestsNow();
                    n2.sendRequestsNow();
                    n3.sendRequestsNow();

                }

                else
                {
                    n3.enqueue ( memupdate );
                    n2.enqueue ( memupdate );
                    n1.enqueue ( memupdate );
                    n0.enqueue ( memupdate );

                    n3.sendRequestsNow();
                    n2.sendRequestsNow();
                    n1.sendRequestsNow();
                    n0.sendRequestsNow();

                }


                try
                {
                    Thread.sleep ( 8L * 1000L );
                }

                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                }

                n0.getIndex().forceNewSearcher();
                n1.getIndex().forceNewSearcher();
                n2.getIndex().forceNewSearcher();
                n3.getIndex().forceNewSearcher();
            }


            clist = n0.getIndex().getMemberships ( node2a.getId(), 0, 99999 );
            System.out.print ( "N0 has n2 membership grant: " + clist.size() );
            printValidMember ( clist );
            clist.close();
            clist = n1.getIndex().getMemberships ( node2a.getId(), 0, 99999 );
            System.out.print ( "N1 has n2 membership grant: " + clist.size() );
            printValidMember ( clist );
            clist.close();
            clist = n2.getIndex().getMemberships ( node2a.getId(), 0, 99999 );
            System.out.print ( "N2 has n2 membership grant: " + clist.size() );
            printValidMember ( clist );
            clist.close();
            clist = n3.getIndex().getMemberships ( node2a.getId(), 0, 99999 );
            System.out.print ( "N3 has n2 membership grant: " + clist.size() );
            printValidMember ( clist );
            clist.close();

            //n0seed.getId()  creates community  com0n0
            //n0seed.getId()  grants  node2a.getId()
            //node2a.getId()  grants  node3b.getId()

            clist = n0.getIndex().getMemberships ( n0seed.getId(), 0, 99999 );
            System.out.print ( "N0 has n0 membership grant: " + clist.size() );
            printValidMember ( clist );
            clist.close();
            clist = n1.getIndex().getMemberships ( n0seed.getId(), 0, 99999 );
            System.out.print ( "N1 has n0 membership grant: " + clist.size() );
            printValidMember ( clist );
            clist.close();
            clist = n2.getIndex().getMemberships ( n0seed.getId(), 0, 99999 );
            System.out.print ( "N2 has n0 membership grant: " + clist.size() );
            printValidMember ( clist );
            clist.close();
            clist = n3.getIndex().getMemberships ( n0seed.getId(), 0, 99999 );
            System.out.print ( "N3 has n0 membership grant: " + clist.size() );
            printValidMember ( clist );
            clist.close();

            clist = n0.getIndex().getMemberships ( com0n0.getDig(), null );
            System.out.println ( "N0: " + clist.size() );
            assertEquals ( 2, clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                co = clist.get ( c );
                assertTrue ( ( long ) co.getPrivateNumber ( CObj.AUTHORITY ) >= CObj.MEMBER_SIMPLE );
                assertEquals ( com0n0.getDig(), co.getPrivate ( CObj.COMMUNITYID ) );
                assertEquals ( "true", co.getPrivate ( CObj.VALIDMEMBER ) );
            }

            clist.close();

            clist = n1.getIndex().getMemberships ( com0n0.getDig(), null );
            System.out.println ( "N1: " + clist.size() );
            assertEquals ( 0, clist.size() );
            clist.close();

            clist = n2.getIndex().getMemberships ( com0n0.getDig(), null );
            System.out.println ( "N2: " + clist.size() );
            assertEquals ( 2, clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                co = clist.get ( c );
                assertTrue ( ( long ) co.getPrivateNumber ( CObj.AUTHORITY ) >= CObj.MEMBER_SIMPLE );
                assertEquals ( com0n0.getDig(), co.getPrivate ( CObj.COMMUNITYID ) );
                assertEquals ( "true", co.getPrivate ( CObj.VALIDMEMBER ) );
            }

            clist.close();

            clist = n3.getIndex().getMemberships ( com0n0.getDig(), null );
            System.out.println ( "N3: " + clist.size() );
            assertEquals ( 2, clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                co = clist.get ( c );
                assertTrue ( ( long ) co.getPrivateNumber ( CObj.AUTHORITY ) >= CObj.MEMBER_SIMPLE );
                assertEquals ( com0n0.getDig(), co.getPrivate ( CObj.COMMUNITYID ) );
                assertEquals ( "true", co.getPrivate ( CObj.VALIDMEMBER ) );
            }

            clist.close();


            clist = n0.getIndex().getMemberships ( node2a.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            System.out.println ( "co.getPrivateNumber(CObj.AUTHORITY): " + co.getPrivateNumber ( CObj.AUTHORITY ) );
            assertEquals ( ( long ) CObj.MEMBER_SIMPLE, ( long ) co.getPrivateNumber ( CObj.AUTHORITY ) );
            assertEquals ( com0n0.getDig(), co.getPrivate ( CObj.COMMUNITYID ) );
            assertEquals ( node3b.getId(), co.getPrivate ( CObj.MEMBERID ) );
            assertEquals ( "true", co.getPrivate ( CObj.VALIDMEMBER ) );
            clist.close();

            clist = n1.getIndex().getMemberships ( node2a.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            assertNull ( co.getPrivateNumber ( CObj.AUTHORITY ) );
            assertNull ( co.getPrivate ( CObj.COMMUNITYID ) );
            assertNull ( co.getPrivate ( CObj.MEMBERID ) );
            assertNull ( co.getPrivate ( CObj.KEY ) );
            clist.close();

            clist = n2.getIndex().getMemberships ( node2a.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            assertEquals ( ( long ) CObj.MEMBER_SIMPLE, ( long ) co.getPrivateNumber ( CObj.AUTHORITY ) );
            assertEquals ( com0n0.getDig(), co.getPrivate ( CObj.COMMUNITYID ) );
            assertEquals ( node3b.getId(), co.getPrivate ( CObj.MEMBERID ) );
            assertEquals ( "true", co.getPrivate ( CObj.VALIDMEMBER ) );
            clist.close();

            clist = n3.getIndex().getMemberships ( node2a.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            assertEquals ( ( long ) CObj.MEMBER_SIMPLE, ( long ) co.getPrivateNumber ( CObj.AUTHORITY ) );
            assertEquals ( com0n0.getDig(), co.getPrivate ( CObj.COMMUNITYID ) );
            assertEquals ( node3b.getId(), co.getPrivate ( CObj.MEMBERID ) );
            assertEquals ( "true", co.getPrivate ( CObj.VALIDMEMBER ) );
            clist.close();

            System.out.println ( "UPDATE SUBSCRIPTION.............................." );
            updatesubs = new CObj();
            updatesubs.setType ( CObj.USR_SUB_UPDATE );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            updatesubs = new CObj();
            updatesubs.setType ( CObj.USR_SUB_UPDATE );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            updatesubs = new CObj();
            updatesubs.setType ( CObj.USR_SUB_UPDATE );
            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            updatesubs = new CObj();
            updatesubs.setType ( CObj.USR_SUB_UPDATE );
            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            clist = n1.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 0, clist.size() );
            clist.close();

            clist = n2.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            assertEquals ( n0seed.getId(), co.getString ( CObj.CREATOR ) );
            assertEquals ( com0n0.getDig(), co.getString ( CObj.COMMUNITYID ) );
            clist.close();

            clist = n3.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 1, clist.size() );
            co = clist.get ( 0 );
            assertEquals ( n0seed.getId(), co.getString ( CObj.CREATOR ) );
            assertEquals ( com0n0.getDig(), co.getString ( CObj.COMMUNITYID ) );
            clist.close();

            System.out.println ( "CREATE SUBSCRIPTION.............................." );
            cb3.oqueue.clear();
            CObj sub3 = new CObj();
            sub3.setType ( CObj.SUBSCRIPTION );
            sub3.pushString ( CObj.CREATOR, node3b.getId() );
            sub3.pushString ( CObj.COMMUNITYID, com0n0.getDig() );
            sub3.pushString ( CObj.SUBSCRIBED, "true" );
            n3.enqueue ( sub3 );

            n3.sendRequestsNow();

            cb3.waitForUpdate();

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            co = n3.getIndex().getSubscription ( com0n0.getDig(), node3b.getId() );
            assertNotNull ( co );
            assertEquals ( com0n0.getDig(), co.getString ( CObj.COMMUNITYID ) );
            assertEquals ( node3b.getId(), co.getString ( CObj.CREATOR ) );
            assertEquals ( "true", co.getString ( CObj.SUBSCRIBED ) );

            System.out.println ( "UPDATE SUBSCRIPTION.............................." );
            updatesubs = new CObj();
            updatesubs.setType ( CObj.USR_SUB_UPDATE );
            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.getIndex().forceNewSearcher();
            n1.getIndex().forceNewSearcher();
            n2.getIndex().forceNewSearcher();
            n3.getIndex().forceNewSearcher();

            clist = n0.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 2, clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                co = clist.get ( c );
                assertNotNull ( n0seed.getId() );
                assertEquals ( com0n0.getDig(), co.getString ( CObj.COMMUNITYID ) );
                assertEquals ( "true", co.getString ( CObj.SUBSCRIBED ) );
            }

            clist.close();

            clist = n1.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 0, clist.size() );
            clist.close();

            clist = n2.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 2, clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                co = clist.get ( c );
                assertNotNull ( n0seed.getId() );
                assertEquals ( com0n0.getDig(), co.getString ( CObj.COMMUNITYID ) );
                assertEquals ( "true", co.getString ( CObj.SUBSCRIBED ) );
            }

            clist.close();

            clist = n3.getIndex().getSubscriptions ( com0n0.getDig(), null );
            assertEquals ( 2, clist.size() );

            for ( int c = 0; c < clist.size(); c++ )
            {
                co = clist.get ( c );
                assertNotNull ( n0seed.getId() );
                assertEquals ( com0n0.getDig(), co.getString ( CObj.COMMUNITYID ) );
                assertEquals ( "true", co.getString ( CObj.SUBSCRIBED ) );
            }

            clist.close();

            System.out.println ( "SEND PRIVATE MESSAGE .............................." );
            CObj prv0 = new CObj();
            prv0.setType ( CObj.PRIVMESSAGE );
            prv0.pushString ( CObj.CREATOR, n0seed.getId() );
            prv0.pushPrivate ( CObj.PRV_RECIPIENT, node2a.getId() );
            prv0.pushPrivate ( CObj.SUBJECT, "pm test subject." );
            prv0.pushPrivate ( CObj.BODY, "pm test body." );
            n0.enqueue ( prv0 );

            try
            {
                Thread.sleep ( 100 );
            }

            catch ( InterruptedException e1 )
            {
                e1.printStackTrace();
            }

            CObj prv1 = new CObj();
            prv1.setType ( CObj.PRIVMESSAGE );
            prv1.pushString ( CObj.CREATOR, node2a.getId() );
            prv1.pushPrivate ( CObj.PRV_RECIPIENT, n0seed.getId() );
            prv1.pushPrivate ( CObj.SUBJECT, "pm test subject." );
            prv1.pushPrivate ( CObj.BODY, "pm test body." );
            n2.enqueue ( prv1 );

            CObj upp = new CObj();
            upp.setType ( CObj.USR_PRVMSG_UPDATE );
            n0.enqueue ( upp );
            n1.enqueue ( upp );
            n2.enqueue ( upp );
            n3.enqueue ( upp );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            String tid = Utils.mergeIds ( n0seed.getId(), node2a.getId() );

            CObjList l = n0.getIndex().getPrvIdent ( n0seed.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            CObj t = l.get ( 0 );
            assertEquals ( "true", t.getPrivate ( CObj.MINE ) );
            assertEquals ( tid, t.getPrivate ( CObj.PRV_MSG_ID ) );
            l.close();

            l = n1.getIndex().getPrvIdent ( n0seed.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            assertNull ( t.getPrivate ( CObj.PRV_RECIPIENT ) );
            l.close();

            l = n2.getIndex().getPrvIdent ( n0seed.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            assertEquals ( node2a.getId(), t.getPrivate ( CObj.PRV_RECIPIENT ) );
            assertEquals ( tid, t.getPrivate ( CObj.PRV_MSG_ID ) );
            l.close();

            l = n3.getIndex().getPrvIdent ( n0seed.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            assertNull ( t.getPrivate ( CObj.PRV_RECIPIENT ) );
            l.close();


            l = n0.getIndex().getPrvMsg ( n0seed.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            String sbj = t.getPrivate ( CObj.SUBJECT );
            String bdy = t.getPrivate ( CObj.BODY );
            assertNotNull ( sbj );
            assertNotNull ( bdy );
            assertEquals ( tid, t.getPrivate ( CObj.PRV_MSG_ID ) );
            l.close();

            l = n1.getIndex().getPrvMsg ( n0seed.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            assertNull ( t.getPrivate ( CObj.SUBJECT ) );
            assertNull ( t.getPrivate ( CObj.BODY ) );
            l.close();

            l = n2.getIndex().getPrvMsg ( n0seed.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            assertEquals ( sbj, t.getPrivate ( CObj.SUBJECT ) );
            assertEquals ( bdy, t.getPrivate ( CObj.BODY ) );
            assertEquals ( "true", t.getPrivate ( CObj.DECODED ) );
            System.out.println ( "SUB: " + sbj + " BDY: " + bdy );
            assertEquals ( tid, t.getPrivate ( CObj.PRV_MSG_ID ) );
            l.close();

            l = n3.getIndex().getPrvMsg ( n0seed.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            assertNull ( t.getPrivate ( CObj.SUBJECT ) );
            assertNull ( t.getPrivate ( CObj.BODY ) );
            l.close();



            l = n0.getIndex().getPrvMsg ( node2a.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            sbj = t.getPrivate ( CObj.SUBJECT );
            bdy = t.getPrivate ( CObj.BODY );
            assertNotNull ( sbj );
            assertNotNull ( bdy );
            System.out.println ( "SUB: " + sbj );
            System.out.println ( "BDY: " + bdy );
            assertEquals ( tid, t.getPrivate ( CObj.PRV_MSG_ID ) );
            l.close();

            l = n1.getIndex().getPrvMsg ( node2a.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            assertNull ( t.getPrivate ( CObj.SUBJECT ) );
            assertNull ( t.getPrivate ( CObj.BODY ) );
            l.close();

            l = n2.getIndex().getPrvMsg ( node2a.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            assertEquals ( sbj, t.getPrivate ( CObj.SUBJECT ) );
            assertEquals ( bdy, t.getPrivate ( CObj.BODY ) );
            assertEquals ( tid, t.getPrivate ( CObj.PRV_MSG_ID ) );
            l.close();

            l = n3.getIndex().getPrvMsg ( node2a.getId(), 0, 99 );
            assertEquals ( 1, l.size() );
            t = l.get ( 0 );
            assertNull ( t.getPrivate ( CObj.SUBJECT ) );
            assertNull ( t.getPrivate ( CObj.BODY ) );
            l.close();


            l = n0.getIndex().getDecodedPrvIdentifiers();
            assertEquals ( 2, l.size() );
            l.close();

            l = n1.getIndex().getDecodedPrvIdentifiers();
            assertEquals ( 0, l.size() );
            l.close();

            l = n2.getIndex().getDecodedPrvIdentifiers();
            assertEquals ( 2, l.size() );
            l.close();

            l = n3.getIndex().getDecodedPrvIdentifiers();
            assertEquals ( 0, l.size() );
            l.close();

            Sort s = new Sort();
            s.setSort ( new SortedNumericSortField ( CObj.docPrivateNumber ( CObj.CREATEDON ), SortedNumericSortField.Type.LONG, true ) );
            l = n0.getIndex().getDecodedPrvMessages ( tid, s );
            assertEquals ( 2, l.size() );
            CObj tu = l.get ( 0 );
            assertEquals ( node2a.getId(), tu.getString ( CObj.CREATOR ) );
            assertEquals ( n0seed.getId(), tu.getPrivate ( CObj.PRV_RECIPIENT ) );
            tu = l.get ( 1 );
            assertEquals ( n0seed.getId(), tu.getString ( CObj.CREATOR ) );
            assertEquals ( node2a.getId(), tu.getPrivate ( CObj.PRV_RECIPIENT ) );
            l.close();

            l = n1.getIndex().getDecodedPrvMessages ( tid, s );
            assertEquals ( 0, l.size() );
            l.close();

            l = n2.getIndex().getDecodedPrvMessages ( tid, s );
            assertEquals ( 2, l.size() );
            tu = l.get ( 0 );
            assertEquals ( node2a.getId(), tu.getString ( CObj.CREATOR ) );
            assertEquals ( n0seed.getId(), tu.getPrivate ( CObj.PRV_RECIPIENT ) );
            tu = l.get ( 1 );
            assertEquals ( n0seed.getId(), tu.getString ( CObj.CREATOR ) );
            assertEquals ( node2a.getId(), tu.getPrivate ( CObj.PRV_RECIPIENT ) );
            l.close();

            l = n3.getIndex().getDecodedPrvMessages ( tid, s );
            assertEquals ( 0, l.size() );
            l.close();


            l = n0.getIndex().getPrivateMsgIdentForIdentity ( n0seed.getId() );
            assertEquals ( 2, l.size() );
            l.close();
            l = n2.getIndex().getPrivateMsgIdentForIdentity ( node2a.getId() );
            assertEquals ( 2, l.size() );
            l.close();
            l = n1.getIndex().getPrivateMsgIdentForIdentity ( n0seed.getId() );
            assertEquals ( 0, l.size() );
            l.close();
            l = n3.getIndex().getPrivateMsgIdentForIdentity ( node2a.getId() );
            assertEquals ( 0, l.size() );
            l.close();


            System.out.println ( "CREATE FILE...................................." );
            cb3.oqueue.clear();

            File tmp = new File ( "testshare" );
            FUtils.deleteDir ( tmp );
            tmp.mkdirs();

            File nf = FUtils.createTestFile ( tmp, 10L * 1024L * 1024L + 263L );

            n3.getShareManager().addShare ( com0n0.getDig(), node3b.getId(),
                                            "testshare", tmp.getPath(), false, false );

            List<DirectoryShare> slst = n3.getShareManager().listShares ( com0n0.getDig(), node3b.getId() );

            for ( int c = 0; c < slst.size(); c++ )
            {
                DirectoryShare ds = slst.get ( 0 );
                System.out.println ( "DS name: " + ds.getShareName() );
            }

            assertEquals ( 1, slst.size() );

            //CObj hf0 = new CObj();
            //hf0.setType ( CObj.HASFILE );
            //hf0.pushString ( CObj.CREATOR, node3b.getId() );
            //hf0.pushString ( CObj.COMMUNITYID, com0n0.getDig() );
            //hf0.pushPrivate ( CObj.LOCALFILE, nf.getPath() );
            //n3.enqueue ( hf0 );

            n3.sendRequestsNow();

            cb3.waitForUpdate();
            o = cb3.oqueue.poll();
            CObj hf0 = ( CObj ) o;
            assertNotNull ( hf0 );
            assertNull ( hf0.getString ( CObj.ERROR ) );


            System.out.println ( "UPDATE HAS FILE .............................." );
            CObj hfupdate = new CObj();
            hfupdate.setType ( CObj.USR_HASFILE_UPDATE );

            n0.enqueue ( hfupdate );
            n1.enqueue ( hfupdate );
            n2.enqueue ( hfupdate );
            n3.enqueue ( hfupdate );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.closeAllConnections();
            n1.closeAllConnections();
            n2.closeAllConnections();
            n3.closeAllConnections();

            n0.enqueue ( hfupdate );
            n1.enqueue ( hfupdate );
            n2.enqueue ( hfupdate );
            n3.enqueue ( hfupdate );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            clst = n0.getIndex().getHasFiles ( com0n0.getDig(), node3b.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clst.size() );
            clst.close();

            clst = n1.getIndex().getHasFiles ( com0n0.getDig(), node3b.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 0, clst.size() );
            clst.close();

            clst = n2.getIndex().getHasFiles ( com0n0.getDig(), node3b.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 0, clst.size() );
            clst.close();

            clst = n3.getIndex().getHasFiles ( com0n0.getDig(), node3b.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clst.size() );
            clst.close();

            System.out.println ( "CREATE POST ................................." );
            cb0.oqueue.clear();
            CObj post = new CObj();
            post.setType ( CObj.POST );
            post.pushString ( CObj.COMMUNITYID, com0n0.getDig() );
            post.pushString ( CObj.CREATOR, n0seed.getId() );
            post.pushString ( CObj.PAYLOAD, "This is a post." );
            n0.enqueue ( post );

            n0.sendRequestsNow();

            cb0.waitForUpdate();
            o = cb0.oqueue.poll();
            post = ( CObj ) o;
            assertNotNull ( post );
            assertNull ( post.getString ( CObj.ERROR ) );

            System.out.println ( "UPDATE POST ................................." );
            CObj pupdate = new CObj();
            pupdate.setType ( CObj.USR_POST_UPDATE );
            n0.enqueue ( pupdate );
            n1.enqueue ( pupdate );
            n2.enqueue ( pupdate );
            n3.enqueue ( pupdate );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.enqueue ( pupdate );
            n1.enqueue ( pupdate );
            n2.enqueue ( pupdate );
            n3.enqueue ( pupdate );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            clst = n0.getIndex().getPosts ( com0n0.getDig(), n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clst.size() );
            clst.close();

            clst = n1.getIndex().getPosts ( com0n0.getDig(), n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 0, clst.size() );
            clst.close();

            clst = n2.getIndex().getPosts ( com0n0.getDig(), n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 0, clst.size() );
            clst.close();

            clst = n3.getIndex().getPosts ( com0n0.getDig(), n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clst.size() );
            clst.close();

            System.out.println ( "DOWNLOAD FILE .............. " + n0seed.getId() );
            File nlf = File.createTempFile ( "download", ".dat" );
            hf0.setType ( CObj.USR_DOWNLOAD_FILE );
            hf0.pushString ( CObj.CREATOR, n0seed.getId() );
            hf0.pushPrivate ( CObj.LOCALFILE, nlf.getPath() );
            n0.enqueue ( hf0 );

            n0.sendRequestsNow();

            try
            {
                Thread.sleep ( 180000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            System.out.println ( "DLFILE: " + nlf.getPath() + " from file: " + nf.getPath() );
            assertTrue ( FUtils.diff ( nf, nlf ) );

            System.out.println ( "UPDATE HAS FILE ................................" );

            n0.enqueue ( hfupdate );
            n1.enqueue ( hfupdate );
            n2.enqueue ( hfupdate );
            n3.enqueue ( hfupdate );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 20000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }


            n0.enqueue ( hfupdate );
            n1.enqueue ( hfupdate );
            n2.enqueue ( hfupdate );
            n3.enqueue ( hfupdate );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 20000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            clst = n0.getIndex().getHasFiles ( com0n0.getDig(), n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clst.size() );
            clst.close();

            clst = n1.getIndex().getHasFiles ( com0n0.getDig(), n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 0, clst.size() );
            clst.close();

            clst = n2.getIndex().getHasFiles ( com0n0.getDig(), n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 0, clst.size() );
            clst.close();

            clst = n3.getIndex().getHasFiles ( com0n0.getDig(), n0seed.getId(), 0, Integer.MAX_VALUE );
            assertEquals ( 1, clst.size() );
            clst.close();

            //subscribe wtih seed0b
            CObj sub0b = new CObj();
            sub0b.setType ( CObj.SUBSCRIPTION );
            sub0b.pushString ( CObj.CREATOR, n0seedb.getId() );
            sub0b.pushString ( CObj.COMMUNITYID, com0n0.getDig() );
            sub0b.pushString ( CObj.SUBSCRIBED, "true" );
            n0.enqueue ( sub0b );

            try
            {
                Thread.sleep ( 2000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            //            //Download a localfile to make sure it's just copied over and not really downloaded.
            //            File tmp2 = new File ( "testshare2" );
            //            FUtils.deleteDir ( tmp2 );
            //            tmp2.mkdirs();
            //
            //            n0.getShareManager().addShare ( com0n0.getDig(), n0seedb.getId(),
            //                                            "testshare2", tmp2.getPath(), false, false );
            //
            //            slst = n0.getShareManager().listShares ( com0n0.getDig(), n0seedb.getId() );
            //
            //            for ( int c = 0; c < slst.size(); c++ )
            //            {
            //                DirectoryShare ds = slst.get ( 0 );
            //                System.out.println ( "DS name: " + ds.getShareName() );
            //            }

            //
            //            assertEquals ( 1, slst.size() );
            //
            //            log.setLevel ( Level.INFO );
            //
            //            hf0.setType ( CObj.USR_DOWNLOAD_FILE );
            //            hf0.pushString ( CObj.CREATOR, n0seedb.getId() );
            //            hf0.pushString ( CObj.SHARE_NAME, "testshare2" );
            //            hf0.getPrivatedata().remove ( CObj.LOCALFILE );
            //            n0.enqueue ( hf0 );
            //
            //            try
            //            {
            //                Thread.sleep ( 10000 );
            //            }

            //
            //            catch ( InterruptedException e )
            //            {
            //                e.printStackTrace();
            //            }

            //
            //            File tf = new File ( "testshare2" + File.separator + nf.getName() );
            //            assertTrue ( tf.exists() );
            //
            //            assertTrue ( FUtils.diff ( nf, tf ) );

            System.out.println ( "TEST SPAM CONTROL.................................." );


            System.out.println ( "CREATE COMMUNITY.............................." );
            cb0.oqueue.clear();
            CObj pubcom = new CObj();
            pubcom.setType ( CObj.COMMUNITY );
            pubcom.pushPrivate ( CObj.NAME, "pubcom" );
            pubcom.pushPrivate ( CObj.DESCRIPTION, "description pubcom" );
            pubcom.pushString ( CObj.CREATOR, n0seed.getId() );
            pubcom.pushString ( CObj.SCOPE, CObj.SCOPE_PUBLIC );
            n0.enqueue ( pubcom );
            cb0.waitForUpdate();
            co = ( CObj ) cb0.oqueue.poll();
            assertNull ( co.getString ( CObj.ERROR ) );

            System.out.println ( "UPDATE COMMUNITY.............................." );
            n1.enqueue ( comupdate );
            n2.enqueue ( comupdate );
            n3.enqueue ( comupdate );

            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }


            comlst = n0.getIndex().getValidCommunities();
            assertEquals ( 2, comlst.size() );
            comlst.close();

            comlst = n1.getIndex().getValidCommunities();
            assertEquals ( 1, comlst.size() );
            comlst.close();

            comlst = n2.getIndex().getValidCommunities();
            assertEquals ( 2, comlst.size() );
            comlst.close();

            comlst = n3.getIndex().getValidCommunities();
            assertEquals ( 2, comlst.size() );
            comlst.close();




            Wrapper.OLDPAYMENT = Wrapper.NEWPAYMENT;
            Wrapper.NEWPAYMENT = 0x0000084000000000L;
            Wrapper.CHECKNEWPAYMENTAFTER = 0;

            createIdentity ( n4, "node4a" );
            n0seed.setType ( CObj.USR_SEED );
            n4.enqueue ( n0seed );

            cb4.waitForUpdate();

            try
            {
                Thread.sleep ( 500 );
            }

            catch ( InterruptedException e1 )
            {
                e1.printStackTrace();
            }


            n4.getIndex().forceNewSearcher();

            n4.enqueue ( updateIdent );
            n4.sendRequestsNow();

            try
            {
                Thread.sleep ( 12L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n4.enqueue ( updateIdent );
            n4.sendRequestsNow();

            try
            {
                Thread.sleep ( 12L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n4.enqueue ( updateIdent );
            n4.sendRequestsNow();

            try
            {
                Thread.sleep ( 12L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            clist = n4.getIndex().getIdentities();
            assertEquals ( 9, clist.size() );
            clist.close();

            //Make sure we DO NOT GET Updates from others yet

            n4.enqueue ( comupdate );

            n4.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n4.enqueue ( comupdate );

            n4.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n4.enqueue ( comupdate );

            n4.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            comlst = n4.getIndex().getValidCommunities();
            assertEquals ( 0, comlst.size() );
            comlst.close();

            System.out.println ( "Add exception." );
            CObj spamex = new CObj();
            spamex.setType ( CObj.SPAMEXCEPTION );
            spamex.pushString ( CObj.CREATOR, n0seed.getId() );
            spamex.pushPrivate ( CObj.STATUS, "save" );
            n0.enqueue ( spamex );

            try
            {
                Thread.sleep ( 5L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            System.out.println ( "REQUEST SPAM UPDATES................................" );
            CObj spr = new CObj();
            spr.setType ( CObj.USR_SPAMEX_UPDATE );

            n1.enqueue ( spr );
            n2.enqueue ( spr );
            n3.enqueue ( spr );
            n4.enqueue ( spr );

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n1.enqueue ( spr );
            n2.enqueue ( spr );
            n3.enqueue ( spr );
            n4.enqueue ( spr );

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n1.enqueue ( spr );
            n2.enqueue ( spr );
            n3.enqueue ( spr );
            n4.enqueue ( spr );

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            comlst = n0.getIndex().getSpamEx ( n0seed.getId(), 0, Long.MAX_VALUE );
            int expsize = comlst.size();
            System.out.println ( "EXSPAM: " + expsize );
            assertTrue ( expsize > 0 );
            comlst.close();
            comlst = n1.getIndex().getSpamEx ( n0seed.getId(), 0, Long.MAX_VALUE );
            assertEquals ( expsize, comlst.size() );
            comlst.close();
            comlst = n2.getIndex().getSpamEx ( n0seed.getId(), 0, Long.MAX_VALUE );
            assertEquals ( expsize, comlst.size() );
            comlst.close();
            comlst = n3.getIndex().getSpamEx ( n0seed.getId(), 0, Long.MAX_VALUE );
            assertEquals ( expsize, comlst.size() );
            comlst.close();
            comlst = n4.getIndex().getSpamEx ( n0seed.getId(), 0, Long.MAX_VALUE );
            assertEquals ( expsize, comlst.size() );
            comlst.close();

            n4.enqueue ( comupdate );

            n4.sendRequestsNow();

            try
            {
                Thread.sleep ( 10L * 1000L );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            comlst = n4.getIndex().getValidCommunities();
            assertEquals ( 1, comlst.size() );
            comlst.close();

            //Submit a post with payment
            cb0.oqueue.clear();
            CObj sub1 = new CObj();
            sub1.setType ( CObj.SUBSCRIPTION );
            sub1.pushString ( CObj.CREATOR, n0seed.getId() );
            sub1.pushString ( CObj.COMMUNITYID, pubcom.getDig() );
            sub1.pushString ( CObj.SUBSCRIBED, "true" );
            n0.enqueue ( sub1 );

            cb0.oqueue.clear();
            CObj sub2 = new CObj();
            sub2.setType ( CObj.SUBSCRIPTION );
            sub2.pushString ( CObj.CREATOR, node3b.getId() );
            sub2.pushString ( CObj.COMMUNITYID, pubcom.getDig() );
            sub2.pushString ( CObj.SUBSCRIBED, "true" );
            n3.enqueue ( sub2 );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();

            updatesubs.setType ( CObj.USR_SUB_UPDATE );
            n0.enqueue ( updatesubs );
            n1.enqueue ( updatesubs );
            n2.enqueue ( updatesubs );
            n3.enqueue ( updatesubs );
            n4.enqueue ( updatesubs );

            n0.sendRequestsNow();
            n1.sendRequestsNow();
            n2.sendRequestsNow();
            n3.sendRequestsNow();
            n4.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            CObj post2 = new CObj();
            post2.setType ( CObj.POST );
            post2.pushString ( CObj.COMMUNITYID, pubcom.getDig() );
            post2.pushString ( CObj.CREATOR, n0seed.getId() );
            post2.pushString ( CObj.PAYLOAD, "This is a post1." );
            post2.pushPrivate ( CObj.PRV_SKIP_PAYMENT, "true" );
            n0.enqueue ( post2 );

            CObj post3 = new CObj();
            post3.setType ( CObj.POST );
            post3.pushString ( CObj.COMMUNITYID, pubcom.getDig() );
            post3.pushString ( CObj.CREATOR, n0seed.getId() );
            post3.pushString ( CObj.PAYLOAD, "This is a post2." );
            n0.enqueue ( post3 );

            n0.enqueue ( pupdate );
            n3.enqueue ( pupdate );

            n0.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.enqueue ( pupdate );
            n3.enqueue ( pupdate );

            n0.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            clst = n3.getIndex().getPosts ( pubcom.getDig(), n0seed.getId(), 0, Long.MAX_VALUE );
            assertEquals ( 1, clst.size() );
            CObj pstt = clst.get ( 0 );
            assertEquals ( "This is a post2.", pstt.getString ( CObj.PAYLOAD ) );
            clst.close();

            //n3 set n0 rank above 5 and rerequests posts.
            //make sure you get the new one.
            CObj setrank = new CObj();
            setrank.setType ( CObj.USR_SET_RANK );
            setrank.pushNumber ( CObj.PRV_USER_RANK, 8 );
            setrank.pushString ( CObj.CREATOR, n0seed.getId() );
            n3.enqueue ( setrank );

            n0.enqueue ( pupdate );
            n3.enqueue ( pupdate );

            n0.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            n0.enqueue ( pupdate );
            n3.enqueue ( pupdate );

            n0.sendRequestsNow();
            n3.sendRequestsNow();

            try
            {
                Thread.sleep ( 10000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

            clst = n3.getIndex().getPosts ( pubcom.getDig(), n0seed.getId(), 0, Long.MAX_VALUE );
            assertEquals ( 2, clst.size() );

            n0.close();
            n1.close();
            n2.close();
            n3.close();
            n4.close();

        }

        catch ( IOException e )
        {
            fail ( "Exception throw: " + e.getMessage() );
            e.printStackTrace();
        }

    }

}
