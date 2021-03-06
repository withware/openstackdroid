package com.rcarrillocruz.android.openstackdroid;

import java.util.Iterator;
import java.util.List;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.rcarrillocruz.android.openstackdroid.json.volume.GetVolumesResponse;
import com.rcarrillocruz.android.openstackdroid.json.volume.VolumeDetailsObject;
import com.rcarrillocruz.android.openstackdroid.model.VolumeModel;

public class VolumeListFragment extends CloudBrowserListFragment {
	List<VolumeModel> volumes;
	private ArrayAdapter<VolumeModel> adapter;
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		volumes = ((CloudBrowserActivity) getActivity()).getVolumes();
        endpoint = ((OpenstackdroidApplication) (getActivity().getApplication())).getVolumeEndpoint();
		
        Intent serviceIntent = new Intent(getActivity(), CloudControllerService.class);
        serviceIntent.setData(Uri.parse(endpoint));
        serviceIntent.putExtra(CloudControllerService.OPERATION, CloudControllerService.GET_VOLUMES_OPERATION);
        serviceIntent.putExtra(CloudControllerService.TOKEN, ((OpenstackdroidApplication) getActivity().getApplication()).getToken());
        serviceIntent.putExtra(CloudControllerService.TENANT, ((OpenstackdroidApplication) getActivity().getApplication()).getTenantId());
        serviceIntent.putExtra(CloudControllerService.RECEIVER, mReceiver); 
        Bundle params = new Bundle();         
        serviceIntent.putExtra(CloudControllerService.PARAMS, params);
                
        getActivity().startService(serviceIntent);
		
        adapter = new ArrayAdapter<VolumeModel>(getActivity(), android.R.layout.simple_list_item_activated_1, volumes);
        setListAdapter(adapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

	}
	
    public void onListItemClick(ListView l, View v, int position, long id) {
		mCurCheckPosition = position;
		getListView().setItemChecked(position, true);
		
		showDetails(position);    
    }

	protected void showDetails(int position) {
		VolumeDetailsFragment sdf = (VolumeDetailsFragment) ((CloudBrowserActivity) getActivity()).getmVolumeDetailsFragment();
		
		if (sdf == null || sdf.getShownIndex() != position) 
			sdf = VolumeDetailsFragment.newInstance(position);
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.item_details, sdf);
        ft.commit();
	        
	    ((CloudBrowserActivity) getActivity()).showDetailsLayout();
	}

	public void onReceiveResult(int resultCode, Bundle resultData) {
		if (resultCode == 200) {
			String operation = resultData.getString(CloudControllerService.OPERATION);
			
			if (operation.equals(CloudControllerService.GET_VOLUMES_OPERATION)) {
				Gson gson = new Gson();
				GetVolumesResponse gvr = gson.fromJson(resultData.getString(CloudControllerService.OPERATION_RESULTS), GetVolumesResponse.class);
				
				populateItems(gvr);
			} 
		}
	}
    
	private void populateItems(GetVolumesResponse gsr) {
		volumes.clear();
		Iterator<VolumeDetailsObject> it = gsr.getVolumes().iterator();
		VolumeDetailsObject item = null;
		
		while(it.hasNext()) {
			item = it.next();		
			VolumeModel newItem = new VolumeModel(item.getId(), item.getDisplay_name(), item.getDisplay_description(), item.getStatus(), item.getSize(), item.getCreated_at(), null);			
			
			if (!item.getAttachments().isEmpty())
				newItem.setAttached_to(item.getAttachments().get(0).getId());
			volumes.add(newItem);
		}
		
		adapter.notifyDataSetChanged();
	}

}
