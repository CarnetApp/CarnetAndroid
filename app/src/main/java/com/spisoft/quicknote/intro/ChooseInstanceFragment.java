package com.spisoft.quicknote.intro;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.spisoft.quicknote.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A simple {@link Fragment} subclass.

 */
public class ChooseInstanceFragment extends Fragment {
    private AsyncTask<Void, Void, String> retrieveInstance;
    private BaseAdapter mAdapter;
    JSONArray mArray = null;

    public ChooseInstanceFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_instance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new BaseAdapter(){
            @Override
            public int getCount() {
                return mArray == null?0:mArray.length();
            }

            @Override
            public Object getItem(int position) {
                try {
                    return mArray.getJSONObject(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.instance_item, parent, false);
                try {
                    JSONObject obj = mArray.getJSONObject(position);
                    ((TextView)convertView.findViewById(R.id.name_tv)).setText(obj.getString("url"));
                    ((TextView)convertView.findViewById(R.id.description_tv)).setText(obj.getString("description"));
                    ((TextView)convertView.findViewById(R.id.register_tv)).setText(obj.getString("registration").equals("open")?R.string.open_for_registrations:R.string.doesnt_accept_new_users);
                    String [] reliArray = getResources().getStringArray(R.array.reliability);
                    int reliability = obj.getInt("reliability");
                    if(reliability<0 || obj.getInt("reliability") > reliArray.length)
                            reliability = reliArray.length-1;
                    ((TextView)convertView.findViewById(R.id.reliability_tv)).setText(getResources().getString(R.string.reliability)+": "+reliArray[reliability]);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return convertView;
            }
        };
        ListView listView = (ListView)view.findViewById(R.id.listView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ((HelpActivity)getActivity()).goToNextcloudFrag(mArray.getJSONObject(position).getString("url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        retrieveInstance = new AsyncTask<Void, Void, String>(){


            @Override
            protected String doInBackground(Void... voids) {
                HttpsURLConnection urlConnection = null;
                String online = "";
                try {

                    URL url = new URL("https://qn.phie.ovh/instances.json");
                    urlConnection = (HttpsURLConnection) url.openConnection();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    online = response.toString();
                    Log.d("online",online);
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(urlConnection!=null)
                        urlConnection.disconnect();
                }
                return online.isEmpty()?"[{url:'carnet.live', description:'An instance dedicated to Carnet with encryption', registration:'open', reliability:'2'}]":online;
            }
            @Override
            protected void onPostExecute(String result) {
                try {
                    mArray = new JSONArray(result);
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        retrieveInstance.execute();
    }

}
