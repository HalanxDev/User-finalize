package com.halanx.userapp.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.GsonBuilder;
import com.halanx.userapp.Fragments.MainFragment;
import com.halanx.userapp.Fragments.StoresFragment;
import com.halanx.userapp.Interfaces.DataInterface;
import com.halanx.userapp.POJO.CartItem;
import com.halanx.userapp.POJO.UserInfo;
import com.halanx.userapp.R;
import com.halanx.userapp.app.Config;
import com.halanx.userapp.util.NotificationUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.halanx.userapp.GlobalAccess.djangoBaseUrl;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    NavigationView navigationView = null;
    Toolbar toolbar = null;

    ImageView userImage;
    TextView nametv;
    Retrofit.Builder builder;
    Retrofit retrofit;
    DataInterface client;

    static String role="none";
    ImageView groupcart;
    ImageView cart, locationButton;
    public static RelativeLayout cartItems;
    public static TextView itemCount;
    public static int backPress =0;
    String group_id = "null";
    static int cartId;
    static int user_id;
    static String group_id1 ;
    EditText groupCode;
    Dialog dialAddMoney;

    static Boolean isaddress = false;
    static String address = " ";
    String token;
    static double promotionalbalance;

    io.socket.client.Socket socket;

    static String first_name;
    private static final String TAG = HomeActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    String cartid;

    List<CartItem> items;
    AppBarLayout barLayout;
    AlertDialog.Builder alertdialog;

    public static int storeID;
    public static String storeName;
    public static String storeLogo;
    public static int storePosition;
    public static String storeCat;
    public static int position = 1;

   //   { try { mSocket = IO.socket("Socket.IO Chat Example"); } catch (URISyntaxException e) {} } @Override public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); mSocket.connect(); }

    private io.socket.client.Socket mSocket;;


    {
        try {
            mSocket = IO.socket("https://api.halanx.com/node/sockets/test/");
        } catch (URISyntaxException e) {
            Log.d("error",e.toString());
        }
    }
    private io.socket.emitter.Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    Log.d("socketmessage", String.valueOf(data));
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    Log.d("socketmessage",message+"     "+username);
                }
            });
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  mSocket.on("new message", onNewMessage);
      //  mSocket.connect();

        setContentView(R.layout.activity_home);

//
//        try {
//            socket = IO.socket("https://api.halanx.com/node/sockets/test/");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
//
//            @Override
//            public void call(Object... args) {
//                socket.emit("foo", "hi");
//                Log.d("messages","data");
//                socket.disconnect();
//            }
//
//        }).on("event", new Emitter.Listener() {
//
//            @Override
//            public void call(Object... args) {
//                Log.d("messages","data");
//            }
//
//        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
//
//            @Override
//            public void call(Object... args) {}
//
//        });
//        socket.connect();
//        Log.d("connectionstatus", String.valueOf(socket.connected()));



        barLayout = (AppBarLayout) findViewById(R.id.app_bar);

        getIntent().setAction("Already created");

        //dialogue box to show uppdate is there on plystore
        if(SplashActivity.flag){
            alertdialog = new AlertDialog.Builder(this);
            alertdialog.setTitle("Update")
                    .setMessage("Update your app to continue with all new features").setCancelable(false)
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.halanx.userapp&hl=en"));
                            startActivity(intent);
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }


        dialAddMoney = new Dialog(HomeActivity.this);
        dialAddMoney.setContentView(R.layout.groupcart);
        groupCode = (EditText) dialAddMoney.findViewById(R.id.group_code);


        token = getApplicationContext().getSharedPreferences("Tokenkey", Context.MODE_PRIVATE).getString("token","token1");
        Log.d("token",token);
        String url = djangoBaseUrl+"users/detail/";

        Volley.newRequestQueue(getApplicationContext()).add(new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    user_id = response.getInt("id");
                    first_name = response.getJSONObject("user").getString("first_name");
                    Log.d("mobilenumber",response.getString("PhoneNo"));
                    getSharedPreferences("Login", Context.MODE_PRIVATE).edit().putString("MobileNumber",response.getString("PhoneNo").trim()).apply();
                    promotionalbalance = response.getDouble("PromotionalBalance");

                    if (response.getString("Address")!="null"){
                        isaddress = true;

                        address = response.getString("Address").trim();
                    }
                    else{
                        address=" ";
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    group_id = response.getString("GroupCart");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Volley.newRequestQueue(getApplicationContext()).add(new JsonObjectRequest(Request.Method.GET, djangoBaseUrl + "carts/detail/", null, new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response123", String.valueOf(response));
                        try
                        {
                            cartId = response.getJSONObject("data").getInt("id");
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(group_id!="null") {
                            if (cartId == Integer.parseInt(group_id))
                            {
                                role = "admin";
                            }
                            else {
                                role = "member";
                            }
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", getApplicationContext().getSharedPreferences("Tokenkey",Context.MODE_PRIVATE).getString("token",null));
                        return params;
                    }

                });


            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", getApplicationContext().getSharedPreferences("Tokenkey",Context.MODE_PRIVATE).getString("token",null));
                return params;
            }

        });









        cart = (ImageView) findViewById(R.id.imageButton_cart);
        locationButton = (ImageView) findViewById(R.id.imageButton_location);
        cart.setOnClickListener(this);
        locationButton.setOnClickListener(this);


        builder = new Retrofit.Builder().baseUrl(djangoBaseUrl).
                addConverterFactory(GsonConverterFactory.create());
        retrofit = builder.build();
        client = retrofit.create(DataInterface.class);

        cartItems = (RelativeLayout) findViewById(R.id.cartitems);
        itemCount = (TextView) findViewById(R.id.itemcount);

        if (position==1||position==3) {

            StoresFragment frg = new StoresFragment();
            frg.passData(getApplicationContext(), itemCount);
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frag_container, frg);
            fragmentTransaction.commit();

        }
        else if(position==2){
            MainFragment frg = new MainFragment();
            frg.passdata(itemCount, String.valueOf(StoresFragment.storesearchdata));
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frag_container, frg);
            fragmentTransaction.commit();

        }


        Call<List<CartItem>> callItems = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(djangoBaseUrl).build().create(DataInterface.class)
                .getUserCartItems(token);
        callItems.enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                List<CartItem> items = response.body();


                Log.d("items", String.valueOf(items));

                if (items != null && items.size() > 0) {
                    //Accesss views?
                    Log.d("itemcount", String.valueOf(items.size()));
                    HomeActivity.cartItems.setVisibility(View.VISIBLE);
                    itemCount.setText(String.valueOf(items.size()));
                } else {
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {

            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //FB LOGIN
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        nametv = (TextView) view.findViewById(R.id.nametv);
        userImage = (ImageView) view.findViewById(R.id.userimage);
        String userInfo = getSharedPreferences("Login", Context.MODE_PRIVATE).getString("UserInfo", null);
        UserInfo user = new GsonBuilder().create().fromJson(userInfo, UserInfo.class);

        nametv.setText("Hi " + getApplicationContext().getSharedPreferences("Login", Context.MODE_PRIVATE).getString("firstname", null) + " !");

        Call<UserInfo> userCall = new Retrofit.Builder().baseUrl(djangoBaseUrl).addConverterFactory(GsonConverterFactory.
                create()).build().create(DataInterface.class).getUserInfo(getApplicationContext().getSharedPreferences("Tokenkey",Context.MODE_PRIVATE).getString("token",null));
        userCall.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                UserInfo info = response.body();
                Log.d("usercart1",String.valueOf(info.getgroupcart()));
                cartid = info.getgroupcart();
                Log.d("usercart1",String.valueOf(info.getgroupcart()));

            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.i("Err",t.toString());
            }
        });


        //IF NO INTERNET
        if (!isNetworkAvailable()) {
            if (!HomeActivity.this.isFinishing()){
                new AlertDialog.Builder(this)
                        .setTitle("No internet connection")
                        .setMessage("You are not connected to the internet").setCancelable(false)
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }

                        })
                        .show();
            }
        }


        if (getSharedPreferences("Login", Context.MODE_PRIVATE).getString("MobileNumber", null) != null) {
            nametv.setText("Hi " + getApplicationContext().getSharedPreferences("Login", Context.MODE_PRIVATE).getString("firstname", null) + " !");

        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("FB_DATA", Context.MODE_PRIVATE);
            String fName = sharedPreferences.getString("fbName", "halanx");
            Log.d("fname", fName);
            String fEmail = sharedPreferences.getString("fbEmail", "halanx");
            Log.d("femail", fEmail);
            String image = sharedPreferences.getString("fbPic", "halanx");
            Log.d("fimage", image);
            nametv.setText(fName);
            Picasso.with(this).load(image).into(userImage);
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    //  txtMessage.setText(message);
                }
            }
        };

        displayFirebaseRegId();


        SharedPreferences sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        final String mobileNumber = sharedPreferences.getString("MobileNumber", null);
        token = getApplicationContext().getSharedPreferences("TokenKey", Context.MODE_PRIVATE).getString("token", null);


        sharedPreferences = getSharedPreferences("status", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("first_login", false)) {

            ViewTarget target = new ViewTarget(R.id.imageButton_location, this);
            ShowcaseView sv = new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setTarget(target)
                    .setContentTitle("DELIVERY LOCATION")
                    .setContentText("Change your delivery location and search for nearby stores by just a singe Tap ")
                    .withHoloShowcase()
                    .setStyle(R.style.CustomShowcaseTheme3)
                    .build();
            sv.setButtonText("OK");



        }
        getSharedPreferences("status", Context.MODE_PRIVATE).edit().
                putBoolean("first_login", false).apply();


        ////////////////  SOCKET IO CONNECTION  ////////////////////



//        Client socket = new Client("https://api.halanx.com/node/sockets/test/", 80);
//        // port kon sa haai ? ab kr call kr error h call me
//        socket.setClientCallback(new Client.ClientCallback () {
//            @Override
//            public void onMessage(String message) {
//
//            }
//
//            @Override
//            public void onConnect(java.net.Socket socket) {
//
//                Log.d("connect","connected");
//
//            }
//            // run kr ?? wait
//            // error aaya hua h
//
//            @Override
//            public void onDisconnect(java.net.Socket socket, String message) {
//
//                Log.d("disconnect",message);
//            }
//
//            @Override
//            public void onConnectError(java.net.Socket socket, String message) {
//
//                Log.d("error",message);
//            }
//        });
//
//        socket.connect();




        Log.d("connection", String.valueOf(mSocket.connected()));
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(position==1){
                backPress++;
                StoresFragment frg = new StoresFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction =
                        getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frag_container, frg);
                fragmentTransaction.commit();
                position=3;
            }
            else if(position==3){
                super.onBackPressed();
                finish();
            }



        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_account) {

            //ADD ACCOUNT PAGE HERE
            startActivity(new Intent(HomeActivity.this, AccountActivity.class));

        } else if (id == R.id.nav_order) {

            startActivity(new Intent(HomeActivity.this, OrdersActivity.class));


//        } else if (id == R.id.nav_payment) {
//
//            startActivity(new Intent(HomeActivity.this, SavedCardsActivity.class));

        } else if (id == R.id.nav_favourites) {
            startActivity(new Intent(HomeActivity.this, FavouritesActivity.class));


        } else if (id == R.id.nav_pts) {
            startActivity(new Intent(HomeActivity.this, Wallet.class));


        } else if (id == R.id.nav_ref) {
            startActivity(new Intent(HomeActivity.this, ReferEarnActivity.class));

        } else if (id == R.id.nav_shopper) {
            startActivity(new Intent(HomeActivity.this, BecomeShopperActivity.class));

        } else if (id == R.id.nav_help) {
            startActivity(new Intent(HomeActivity.this, HelpActivity.class));

        }
        else if (id == R.id.subscription) {

            startActivity(new Intent(HomeActivity.this, SubscriptionDrawerActivity.class));

        }
        else if (id == R.id.group_order) {

            final String url = djangoBaseUrl+"carts/creategroup/";




            LinearLayout jointhegroup = (LinearLayout) dialAddMoney.findViewById(R.id.jointhe_group);
            LinearLayout join = (LinearLayout) dialAddMoney.findViewById(R.id.joined);
            LinearLayout not_join = (LinearLayout) dialAddMoney.findViewById(R.id.not_joined);
            Button joingroup = (Button) dialAddMoney.findViewById(R.id.join);
            Button creategroup = (Button) dialAddMoney.findViewById(R.id.creategroup);
            final Button share = (Button) dialAddMoney.findViewById(R.id.share);
            View tv_or =(View) dialAddMoney.findViewById(R.id.or);

            Button gotocart = (Button) dialAddMoney.findViewById(R.id.cartjoin);

            String groupdata = getApplicationContext().getSharedPreferences("groupData",Context.MODE_PRIVATE).getString("goupcode","");

            if (!groupdata.equals("")){
                groupCode.setText(groupdata.trim());
            }

            Log.d("group id", String.valueOf(group_id));
            if(String.valueOf(group_id).trim()!="null") {
                join.setVisibility(View.VISIBLE);
                not_join.setVisibility(View.GONE);
                jointhegroup.setVisibility(View.GONE);
                tv_or.setVisibility(View.GONE);
            }
            else
            {
                join.setVisibility(View.GONE);
                not_join.setVisibility(View.VISIBLE);

            }

            gotocart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(HomeActivity.this,CartActivity.class));
                    dialAddMoney.dismiss();

                }
            });

            joingroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!groupCode.getText().toString().trim().equals("")) {

                        Volley.newRequestQueue(getApplicationContext()).add(new JsonObjectRequest(Request.Method.GET, djangoBaseUrl + "carts/joingroup/" + groupCode.getText().toString().trim() + "/", null, new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                startActivity(new Intent(HomeActivity.this,CartActivity.class));
                                role = "member";
                                group_id1=groupCode.getText().toString().trim();
                                getApplicationContext().getSharedPreferences("groupCode",Context.MODE_PRIVATE).edit().putString("groupcode",group_id1).apply();

                                dialAddMoney.dismiss();
                            }
                        }, new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("error", String.valueOf(error));
                                Toast.makeText(getApplicationContext(), "Invalid Code", Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Content-Type", "application/json");
                                params.put("Authorization", getApplicationContext().getSharedPreferences("Tokenkey", Context.MODE_PRIVATE).getString("token", null));
                                return params;
                            }

                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid Code", Toast.LENGTH_SHORT).show();
                    }
                }

            });



            creategroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Volley.newRequestQueue(getApplicationContext()).add(new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                group_id1 = response.getString("id");
                                Log.d("group_id", String.valueOf(group_id1));
                                getApplicationContext().getSharedPreferences("groupCode",MODE_PRIVATE).edit().putString("groupcode",group_id1).apply();

                                role = "admin";
                                getApplicationContext().getSharedPreferences("groupcart", Context.MODE_PRIVATE).edit().putBoolean("groupcart", true).apply();
                                startActivity(new Intent(HomeActivity.this, CartActivity.class).putExtra("group_id", group_id1));
                                dialAddMoney.dismiss();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("error", String.valueOf(error));
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/json");
                            params.put("Authorization", getApplicationContext().getSharedPreferences("Tokenkey", Context.MODE_PRIVATE).getString("token", null));
                            return params;
                        }

                    });
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

                    sharingIntent.setType("text/plain");
                    String shareBody = "Get grocery and food delivered from your favorite Stores and " +
                            "restaurants in as little as an hour. Download app now : " +
                            "https://play.google.com/store/apps/details?id=com.halanx.userapp";

                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Halanx : Grocery and Food delivery");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));

                }
            });

            dialAddMoney.show();


        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onClick(final View view) {
        int id = view.getId();
        switch (id) {

            case R.id.imageButton_cart:

                startActivity(new Intent(HomeActivity.this, CartActivity.class));
                getApplicationContext().getSharedPreferences("groupcart", Context.MODE_PRIVATE).edit().putBoolean("groupcart", false).apply();
                break;

            case R.id.imageButton_location:
                startActivity(new Intent(HomeActivity.this, MapsActivity.class));
                break;


        }
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        String url = ""+djangoBaseUrl+"users/detail/";
        final String finalToken = getSharedPreferences("Tokenkey",Context.MODE_PRIVATE).getString("token",null);


        Log.i("Gcm", url);
        Log.i("Gcm", finalToken);
        JSONObject obj = new JSONObject();
        try {
            obj.put("GcmId", regId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.PATCH, url, obj, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("GcmId", "Done");

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                Log.d("token",finalToken);
                params.put("Authorization", finalToken);
                return params;
            }
        });

        Log.e(TAG, "Firebase reg id: " + regId);

//        if (!TextUtils.isEmpty(regId))
//      //      txtRegId.setText("Firebase Reg Id: " + regId);
//        else//    txtRegId.setText("Firebase Reg Id is not received yet!");
    }

    @Override
    protected void onResume() {
        super.onResume();


        Call<List<CartItem>> callItems = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(djangoBaseUrl).build().create(DataInterface.class)
                .getUserCartItems(token);
        callItems.enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                List<CartItem> items = response.body();


                Log.d("items", String.valueOf(items));

                if (items != null && items.size() > 0) {
                    //Accesss views?
                    Log.d("itemcount", String.valueOf(items.size()));
                    HomeActivity.cartItems.setVisibility(View.VISIBLE);
                    itemCount.setText(String.valueOf(items.size()));
                } else {
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {

            }
        });


        String action = getIntent().getAction();
        // Prevent endless loop by adding a unique action, don't restart if action is present
        if(action == null || !action.equals("Already created")) {
            Log.v("Example", "Force restart");
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        // Remove the unique action so the next time onResume is called it will restart
        else
            getIntent().setAction(null);

        super.onResume();
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();

        getApplicationContext().getSharedPreferences("groupData",Context.MODE_PRIVATE).edit().putString("goupcode",groupCode.getText().toString().trim()).apply();

    }

    //Check internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
