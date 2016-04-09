package com.yadercenteno.libretaclaves;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.yadercenteno.libretaclaves.Constantes.COLUMNA_ID;
import static com.yadercenteno.libretaclaves.Constantes.COLUMNA_NOMBRE;

public class ListadoAdaptador extends BaseAdapter
{
	public ArrayList<HashMap<String,String>> list;
	Activity activity;
    Context c;

	public ListadoAdaptador(Activity activity, ArrayList<HashMap<String, String>> list) {
		super();
		this.activity = activity;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	private class ViewHolder {
		   TextView txtID1;
	       TextView txtNombre;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater =  activity.getLayoutInflater();

        HashMap<String, String> map = list.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fila_listado, null);

            holder = new ViewHolder();
            holder.txtID1 = (TextView) convertView.findViewById(R.id.txtID1);
            holder.txtNombre = (TextView) convertView.findViewById(R.id.txtNombre);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtID1.setText(map.get(COLUMNA_ID));
        holder.txtNombre.setText(map.get(COLUMNA_NOMBRE));

        return convertView;
	}
}
