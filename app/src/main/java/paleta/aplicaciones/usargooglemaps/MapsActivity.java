package paleta.aplicaciones.usargooglemaps;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements View.OnClickListener ,AdapterView.OnItemClickListener {
    String apiKeyMapsDirections="AIzaSyB5IFRrtueth7Ycsz1qmRro3gIzMQBJUyw"; //api key tipo server de tu Google Console
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker myMarker;

    private static final LatLng LOWER_MANHATTAN = new LatLng(40.722543,
            -73.998585);
    private static final LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);
    private static final LatLng WALL_STREET = new LatLng(40.7064, -74.0094);

    private double longitud;
    private double latitud;
    private EditText etBusqueda;
    private Button btnBuscar, btnRestaurantesCercanos,btnCercanoClave;
    private Location localizacionAutom;


    private static final String TAG = "TAG";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TIPO_AUTOCOMPLETADO = "/autocomplete";
    private static final String FORMATO_ENVIO = "/json";

    private static final String API_KEY = "AIzaSyB5IFRrtueth7Ycsz1qmRro3gIzMQBJUyw"; //tu api key del tipo servidor

    private static ArrayList<String> placesID;
private AutoCompleteTextView autoCompView;
    private String placeIDSeleccionado;
    private boolean seleccionoAutocompletar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

       // etBusqueda = (EditText) findViewById(R.id.etBusqueda);
        btnBuscar= (Button) findViewById(R.id.btnBuscar);
        btnCercanoClave= (Button) findViewById(R.id.btnCercanoClave);
        btnRestaurantesCercanos= (Button) findViewById(R.id.btnRestaurantesCercanos);

        setUpMapIfNeeded();
        btnBuscar.setOnClickListener(this);
        btnRestaurantesCercanos.setOnClickListener(this);
        btnCercanoClave.setOnClickListener(this);

        autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        autoCompView.setAdapter(new AdaptadorAutocompletar(this, R.layout.listado_de_lugares));
        autoCompView.setOnItemClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                Toast.makeText(
                        getBaseContext(), "Hubo cambios "+location.getLatitude(), Toast.LENGTH_SHORT).show();

                ubicarEnPosicion(location);
                latitud=location.getLatitude();
                longitud=location.getLongitude();
                localizacionAutom= location;
                Log.d("UBICACION", "latitus "+ latitud +" longitud " + longitud);
            }
        };

        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);


        setUpMapIfNeeded();


        MarkerOptions options = new MarkerOptions();
        options.position(LOWER_MANHATTAN);
        options.position(BROOKLYN_BRIDGE);
        options.position(WALL_STREET);
        mMap.addMarker(options);

        String url = getMapsApiDirectionsUrl();
        ArrayList<String> opciones= new ArrayList<String>();
        opciones.add(url);
        opciones.add("2");
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(opciones);


        addMarkers();

        /*para obtener el place ID seria en el metodo 1 pero es algo complicado con ciertos lugares,
        seria mejor agregar un autocompletar
        */
        ArrayList<String> opciones2= new ArrayList<String>();
        String urlPlaceID="https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJwbvo6Sf614UREMa5Heqfrtk&key=AIzaSyB5IFRrtueth7Ycsz1qmRro3gIzMQBJUyw";
        opciones2.add(urlPlaceID);//1er param la url
        opciones2.add("3");
        ReadTask downloadTask2 = new ReadTask();
        downloadTask2.execute(opciones2);





    }

    /***
        ==== METODOS PARA EL TRAZADO DE UNA RUTA
     ****/

    private String getMapsApiDirectionsUrl() {


        //envio solicitud a Google Apis para ruta


        //para solicitar el trazo a la api es https://developers.google.com/maps/documentation/directions/intro

       // mi casa   22.252734, -97.835301


       //tec madero 22.2519514,-97.8458526
     //53733, -97.848258

      /*  String waypoints = "origin=22.252734,-97.8487428&destination=22.2519514,-97.8458526&key="+apiKeyMapsDirections+"&waypoints=optimize:true|"
                + LOWER_MANHATTAN.latitude + "," + LOWER_MANHATTAN.longitude
                + "|" + "|" + BROOKLYN_BRIDGE.latitude + ","
                + BROOKLYN_BRIDGE.longitude + "|" + WALL_STREET.latitude + ","
                + WALL_STREET.longitude;
                */

        String waypoints = "origin=22.2518269,-97.834871&destination=22.253733,-97.848258&key="+apiKeyMapsDirections+"";

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";


        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;


       // url="https://maps.googleapis.com/maps/api/directions/json?origin=Brooklyn&destination=Queens&mode=transit&key="+apiKeyMapsDirections;


        Log.d("SOLICITUD", "" + url);
        return url;
    }

    private void addMarkers() {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(BROOKLYN_BRIDGE)
                    .title("First Point"));
            mMap.addMarker(new MarkerOptions().position(LOWER_MANHATTAN)
                    .title("Second Point"));
            mMap.addMarker(new MarkerOptions().position(WALL_STREET)
                    .title("Third Point"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) { //obtengo el id de la vista en este caso Button
            case R.id.btnBuscar: //si el ID es igual al de btnBuscar de la clase R entonces
               //String buscarDireccion=etBusqueda.getText().toString().trim();
                String buscarDireccion=autoCompView.getText().toString().trim();
                //establezco URL para conectar al webservice de  Google Apis Geocode
                buscarDireccion=buscarDireccion.replaceAll(" ", "%20");
                String url =
                        "https://maps.googleapis.com/maps/api/geocode/json?address="+buscarDireccion+"&languague=ES&key=" + apiKeyMapsDirections;
                //hacer conexion url en segundo plano por medio de AsyncTask
                Log.d("URL", url);
                ArrayList<String> opciones= new ArrayList<String>();
                opciones.add(url);//1er param la url
                opciones.add("1");/*
                1 -singinica que al final del hilo obtendre los datos para el geocode.
                2- Trazo de Recorrido (origen-destino)
                */
                ReadTask downloadTask = new ReadTask();
                downloadTask.execute(opciones);
                break;

            case R.id.btnRestaurantesCercanos:

                if(localizacionAutom!=null){
                    //si tenemos ubicacion entonces buscar resturantes cercanos
                    restaurantesCercanos(latitud,longitud);
                }

            break;

            case R.id.btnCercanoClave:
               // String buscarClave=etBusqueda.getText().toString().trim();
                String buscarClave=autoCompView.getText().toString().trim();
                buscarClave=buscarClave.replaceAll(" ", "%20");
                    cercanoClave(latitud, longitud, buscarClave);
                Log.d("BUSCAR", "BUSCANDO "+buscarClave);
                break;


        }

    }




    //ASYNC TASK de Lectura de url
    private class ReadTask extends AsyncTask< ArrayList<String>, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... datos) {


            try {
                String url= datos[0].get(0);
                HttpConnection http = new HttpConnection();
                String respuesta = http.readUrl(datos[0].get(0));
                Log.d("URL", ""+url);
                Log.d("RESPUESTA", ""+respuesta);
                datos[0].set(0,respuesta); //modifico el 2do elemento del array list con el de la respuesta del readURL

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return datos[0];
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            String respuestaURL=result.get(0);
            super.onPostExecute(result);
            int tipoConsulta=Integer.parseInt(result.get(1));
            switch(tipoConsulta){
                case 1:
                    //quiero obtener los datos de geocoder (direccion a latitud y longitud para luego establecerlos en el mapa)
                    Log.d("RESPUESTA", ""+respuestaURL);
                    getJsonResponse(respuestaURL,tipoConsulta);
                    break;
                case 2:
                    //trazar ruta de punto origen a destino en mapa.
                    new ParserTask().execute(respuestaURL);
                    break;
                case 3:
                    Log.d("INFORMACION", ""+ respuestaURL);
                    getJsonResponse(respuestaURL,tipoConsulta );
                    break;
            }



        }
    }



    public void getJsonResponse(String result,int tipoConsulta) {
        //from 0 = sql, 1 = web
        JSONObject jsonField = null;
        try {
            jsonField = new JSONObject(result); //parseo string foramto json a un objeto JSON
            String jsonResponse=result;

            switch(tipoConsulta){
                case 1:
                    JSONArray datos = jsonField.getJSONArray("results");
                    Log.d("VALORES","datos son  "+datos);
            /*Del json que me proporciona google busco el array "results"
            */
                    if (datos != null) {

                        Log.d("VALORES","datos no son nullo "+datos);
                       /*
                        for (int i = 0; i < datos.length(); i++) {

                        }
                        */
                        if(datos.length()>0){
                           /*como solo estamos buscando la mejor coincidencia optare por el primer indice
                            si deseas obtener todas las coindicencia seria necesario recorrer el for
                            */

                            /*ahora estando en el 1er results, ir a geomretry->location->
                                en este encontramos la latitud y longitud (lat , lng)
                             */


                            String  cadenaGeometry=datos.getJSONObject(0).getString("geometry").toString(); //me posiciono en la llave geometry
                            Log.d("VALORES","geometry es "+cadenaGeometry);
                            JSONObject geometry = new JSONObject(cadenaGeometry);
                            String cadenaLocation=geometry.get("location").toString();//me posiciono en location
                            Log.d("VALORES","location es "+cadenaLocation);
                            JSONObject location = new JSONObject(cadenaLocation);

                            String latitud=location.get("lat").toString();//me posiciono en location
                            String longitud=location.get("lng").toString();

                            Log.d("VALORES", "la latitud es " + latitud + "la longitud es "+ longitud );
                            Toast.makeText(getApplicationContext(),"la latitud es " + latitud + "la longitud es "+ longitud, Toast.LENGTH_LONG).show();
                            LatLng localizacion = new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud)); //convierto de string a Double las latitud y longitud
                            ubicarEnPosicion(localizacion);


                            //luego con el placeID obtener toda la informacion del lugar
                            String placeID="";
                            if(seleccionoAutocompletar)
                            {
                                //si fue por un select de autocompletar mejor usar ese, es mas exacto que el geocode inverso
                                placeID= placeIDSeleccionado;
                            }else{
                                placeID=datos.getJSONObject(0).getString("place_id").toString();
                            }
                            Log.d(TAG,"METODO PlaceID "+ placeID);
                            ArrayList<String> opciones2= new ArrayList<String>();
                            String urlPlaceID="https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeID+"&key=AIzaSyB5IFRrtueth7Ycsz1qmRro3gIzMQBJUyw";
                            opciones2.add(urlPlaceID);//1er param la url
                            opciones2.add("3");
                            ReadTask downloadTask2 = new ReadTask();
                            downloadTask2.execute(opciones2);
                            //terminado el proceso hacerlo de nuevo false el autocompletar asi como el placeID a vacio
                            seleccionoAutocompletar=false;
                            placeIDSeleccionado="";

                        }

                    }else{

                    }
                    break;


                case 3:
                        ///apartir del place Id

                    //en esta parte validar si existe o no el jsonobject para evitar errores
                    JSONObject resultado= jsonField.getJSONObject("result"); //aqui es result en vez de results
                    String direccionCompleta=resultado.getString("formatted_address");
                    String numeroTelefonico=resultado.getString("formatted_phone_number");
                    String urlIcono=resultado.getString("icon");
                    String rating=resultado.getString("rating");
                    String categorias=resultado.getString("types");
                    String sumatoriaRating=resultado.getString("user_ratings_total");
                    String urlSitioWeb=resultado.getString("website");
                    String nombreDelLugar=resultado.getString("name");

                    Toast.makeText(getApplicationContext(),"Datos de la ubicacion son \n"+
                            "DIRECCION "+direccionCompleta +
                    "NUMERO "+numeroTelefonico +
                    "ICONO "+urlIcono +
                    "RATING "+rating +
                    "CATEGORIAS "+categorias +
                    "SUMATORIA "+sumatoriaRating +
                    "WEBSITE "+urlSitioWeb +
                    "NombreLugar " + nombreDelLugar, Toast.LENGTH_LONG).show();

                    Log.d("DIRECCION", "" + direccionCompleta);
                    Log.d("NUMERO", ""+numeroTelefonico);
                    Log.d("ICONO", ""+urlIcono);
                    Log.d("RATING", ""+rating);
                    Log.d("CATEGORIAS", ""+categorias);
                    Log.d("SUMATORIA", ""+sumatoriaRating);
                    Log.d("WEBSITE", ""+urlSitioWeb);
                    Log.d("NombreLugar", ""+nombreDelLugar);
                    break;
            }



            }
        catch (JSONException e1)
        {
            e1.printStackTrace();
            Log.e("ERROR", "error en "+ e1.toString());
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(2);
                polyLineOptions.color(Color.BLUE);
            }

            mMap.addPolyline(polyLineOptions);
        }
    }
    /*** END Metodos Trazados RUta***/




    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        setMarkerConClic();


        mMap.setOnMyLocationChangeListener(myLocationChangeListener);

        //Agrego otro marker pero de difernet color
        myMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(8, 8))
                .title("My Spot")
                .snippet("This is my spot!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

    }


    public void setMarkerConClic(){
        // Setting a click event handler for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                // Clears the previously touched position
                // mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);
            }
        });
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            myMarker = mMap.addMarker(new MarkerOptions().position(loc));
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
        }
    };

    /***
     *
     * Funcion ubicarEnPosicion - Establace un marker color Verde y te envía a la ubicacion que fue recibida
     * @param loc  - Objeto tipo LatLng (el cual tiene que deseeamo mostrar en el mapa
     *
     *
     */
    public void ubicarEnPosicion(LatLng loc){
        if(loc != null) {  //mientras si haya tenido un location

         /*==PARA EL MOVIEMIENTO DE LA CAMARA, es decir se posicione donde debe ser*/
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(loc)      // Establece como centro del mapa la localizacion recibida
                    .zoom(13)        //Establece la cantidad de zoom
                   // .bearing(90)   //Establece la orientacion de la camara en X grados.
                  //  .tilt(0)                   // Establece el titl de la camara a X degrees
                    .build();                   // Crea un  CameraPosition para el  builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            //para agregar un marker al lugar


            mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title("Busqueda exitosa " + autoCompView.getText().toString().trim()));
                   // .title("Busqueda exitosa " + etBusqueda.getText().toString()));
        }
    }

    /***
     *
     * Funcion ubicarEnPosicion - Establace un marker color Rojo y te envía a la ubicacion que fue recibida
     * @param loc  - Objeto tipo Location
     *
     *
     */
    public void ubicarEnPosicion(Location loc){
        if(loc != null) {  //mientras si haya tenido un location

         /*==PARA EL MOVIEMIENTO DE LA CAMARA, es decir se posicione donde debe ser*/
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(loc.getLatitude(), loc.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(loc.getLatitude(), loc.getLongitude()))      // Sets the center of the map to location user
                    .zoom(13)                   // Sets the zoom
                            // .bearing(90)                // Sets the orientation of the camera to X grades
                            //  .tilt(0)                   // Sets the tilt of the camera to X degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            //para agregar un marker al lugar
            mMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude())).title("Mi ubicacion actual"));
        }
    }

    public void restaurantesCercanos(double latitud,double longitud){
       /*Opcion para buscar restaurantes cercanos a donde te ubicas conforme la latitud y longitud*/
        Uri gmmIntentUri = Uri.parse("geo:"+latitud+","+longitud+"?q=restaurants");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void cercanoClave(double latitud,double longitud, String busqueda){
        //probar con palabra escuelas
       /*Opcion para buscar lo que sea (String busqueda) cercanos a donde te ubicas conforme la latitud y longitud*/
        Uri gmmIntentUri = Uri.parse("geo:"+latitud+","+longitud+"?q="+busqueda+"");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }


    /* ===  SECCION AUTOCOMPLETE====*/
    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();

        /*como hizo click en algunas de las posibles direcciones entonces
        obtnego el placeID del lugar selecto para que al dar en el btn buscar ya tengo el placeID correcto
         */
        seleccionoAutocompletar=true;
        placeIDSeleccionado=placesID.get(position).toString();

    }
    public ArrayList autocompletar(String input) {
        ArrayList resultList = null;

        seleccionoAutocompletar=false;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TIPO_AUTOCOMPLETADO + FORMATO_ENVIO);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:mx");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            Log.d(TAG,"URL enviada fue "+ url);
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];

            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            //Iniciar mi arraylist de places
            placesID = new ArrayList<String>();


            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                placesID.add(predsJsonArray.getJSONObject(i).getString("place_id"));//asigno cada valor del place ID
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Eror JSON" + e.toString() );
        }

        return resultList;
    }


    /*clase de autocompletado Adaptador*/
    class AdaptadorAutocompletar extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public AdaptadorAutocompletar(Context context, int textViewResourceId) {
            super(context, textViewResourceId);//llamando contructor de clase padre ArrayAdapter
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {

            return resultList.get(index).toString();
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults resultadosDelFiltro = new FilterResults();
                    if (constraint != null) {
                        // Regresa los resultado del autocomplete
                        resultList = autocompletar(constraint.toString());

                        // Asgianr los datos al FilterResults
                        resultadosDelFiltro.values = resultList;
                        resultadosDelFiltro.count = resultList.size();
                    }
                    return resultadosDelFiltro;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}
