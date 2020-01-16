package com.neobit.maximseg

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beust.klaxon.Klaxon
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.data.model.Horario
import com.neobit.maximseg.data.model.Punto
import com.neobit.maximseg.data.model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.textNombre
import kotlinx.android.synthetic.main.fragment_profile.card_inicio
import kotlinx.android.synthetic.main.fragment_profile.card_salida
import kotlinx.android.synthetic.main.fragment_profile.card_punto
import kotlinx.android.synthetic.main.fragment_profile.profilePicture
import kotlinx.android.synthetic.main.fragment_profile.*



class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var usuario: User
    private lateinit var punto: Punto
    private lateinit var horario: Horario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).setBar("Maximseg",false)

        prefs = PreferenceManager.getDefaultSharedPreferences(this@ProfileFragment.context)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onResume() {
        super.onResume()
        prefs = PreferenceManager.getDefaultSharedPreferences(this@ProfileFragment.context)
        usuario = Klaxon().parse<User>(prefs.getString("guardias", ""))!!
        textNombre.text = "${usuario.nombres} "
        punto = Klaxon().parse<Punto>(prefs.getString("punto",""))!!
        card_punto.text = punto.codigo
        horario = Klaxon().parse<Horario>(prefs.getString("horario",""))!!
        card_salida.text = horario.hora_fin
        card_inicio.text = horario.hora_inicio

        if (usuario.imagen.isNotEmpty())
            Picasso.get().load(Utils.URL_MEDIA + usuario.imagen).error(R.drawable.men).placeholder(R.drawable.men).noFade().into(profilePicture)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {

            }
    }
}
