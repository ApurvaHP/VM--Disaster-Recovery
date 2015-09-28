import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import CONFIG1.*;

import com.oracle.webservices.internal.literal.ArrayList;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class collect1stats {
	
	ServiceInstance si = null;
	String vmname;

	public collect1stats(String vmname) {
		this.vmname = vmname;

	}

	public void pingvms() throws IOException {
		URL url = new URL(Config.getVmwareHostURL());
		ServiceInstance si = new ServiceInstance(url, Config.getVmwareLogin(),
				Config.getVmwarePassword(), true);
		Folder rootFolder = si.getRootFolder();
		String rootname = rootFolder.getName();
		
		VirtualMachine host = (VirtualMachine) new InventoryNavigator(
				si.getRootFolder()).searchManagedEntity("VirtualMachine",
				vmname);

		
		System.out.println("Collecting stats for vm : " + host.getName());
		Runnable task = new statsVM(host.getName());
		System.out.println(host.getName());
		Thread worker = new Thread(task);
		worker.start();
		
	}

	
	public static void main(String[] args) throws Exception {
		
		if(args.length!=0){
		
		collect1stats obj = new collect1stats(args[0]);
		obj.pingvms();
		}
	}
}
