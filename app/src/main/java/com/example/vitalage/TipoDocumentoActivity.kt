package com.example.vitalage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.allViews
import com.example.vitalage.databinding.TipoDeDocumentoBinding

class TipoDocumentoActivity : AppCompatActivity(), AdapterView.OnItemClickListener{

    private var listView : ListView?  = null
    private var arrayAdapter:ArrayAdapter<String>? = null
    private lateinit var binding: TipoDeDocumentoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = TipoDeDocumentoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        listView = findViewById(R.id.lista)
        arrayAdapter = ArrayAdapter(applicationContext,android.R.layout.simple_list_item_single_choice,resources.getStringArray(R.array.Tipos_de_documentos))
        listView?.adapter = arrayAdapter
        listView?.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView?.onItemClickListener = this

        binding.flechitaatras.setOnClickListener(){
            val intent = Intent(this,RegistarseActivityActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item:String = parent?.getItemAtPosition(position).toString()
        val checked = listView?.isItemChecked(position)
        if (checked!!){
            listView?.setItemChecked(position,true)
        }else{
            listView?.setItemChecked(position,false)
        }
        val intent = Intent(this, RegistarseActivityActivity::class.java)
        intent.putExtra("tipo_documento",item)
        startActivity(intent)
    }

}