package com.halanx.userapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.halanx.userapp.Activities.ItemDisplayActivity;
import com.halanx.userapp.Interfaces.DataInterface;
import com.halanx.userapp.POJO.CartItem;
import com.halanx.userapp.POJO.CartItemPost;
import com.halanx.userapp.POJO.ProductInfo;
import com.halanx.userapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.halanx.userapp.GlobalAccess.djangoBaseUrl;

/**
 * Created by samarthgupta on 23/05/17.
 */

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductInfo> products = new ArrayList<>();
    private static int restQuantity[];
    private Context c;
    private String storeCategory;
    public String mobileNumber;

    public ProductAdapter(List<ProductInfo> products, Context c, String storeCat, String mobileNumber) {
        this.products = products;
        this.c = c;
        storeCategory = storeCat;
        this.mobileNumber = mobileNumber;

        restQuantity = new int[products.size()];
        for (int i = 0; i < products.size(); i++) {
            restQuantity[i] = 1;
        }
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_product_recycler, parent, false);
        ProductViewHolder holder = new ProductViewHolder(view, products, c);
        return holder;

    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {

        if (storeCategory.equals("Grocery")) {

            holder.cvProducts.setVisibility(View.VISIBLE);
            holder.cvRest.setVisibility(View.GONE);
            Picasso.with(c).load(products.get(position).getProductImage()).into(holder.productImage);
            holder.productName.setText(products.get(position).getProductName());
            holder.productPrice.setText("Rs." + String.valueOf(products.get(position).getPrice()));
        } else if (storeCategory.equals("Food")) {
            //Picasso.with(c).load(R.drawable.fav_48).into(holder.productImage);
            holder.cvProducts.setVisibility(View.GONE);
            holder.cvRest.setVisibility(View.VISIBLE);
            holder.tvRestName.setText(products.get(position).getProductName());
            holder.tvRestPrice.setText("Rs." + String.valueOf(products.get(position).getPrice()));
            holder.etRestQuan.setText(String.valueOf(restQuantity[position]));
        }


    }

    @Override
    public int getItemCount() {

        return products.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cvProducts, cvRest;
        ImageView productImage;
        TextView productName, productPrice;
        List<ProductInfo> products;
        Context c;

        //For rest
        TextView tvRestName, tvRestPrice;
        CardView cvAddCart;
        EditText etRestQuan;
        RelativeLayout rvInc, rvDec;
        TextView tvCart;
        ImageView ivFav;


        public ProductViewHolder(View itemView, List<ProductInfo> products, Context c) {
            super(itemView);
            this.products = products;
            this.c = c;


            cvProducts = (CardView) itemView.findViewById(R.id.cvProducts);
            cvRest = (CardView) itemView.findViewById(R.id.cvRest);

            productImage = (ImageView) itemView.findViewById(R.id.itemImage);
            productName = (TextView) itemView.findViewById(R.id.itemName);
            productPrice = (TextView) itemView.findViewById(R.id.itemPrice);

            tvRestName = (TextView) itemView.findViewById(R.id.restName);
            tvRestPrice = (TextView) itemView.findViewById(R.id.restPrice);
            cvAddCart = (CardView) itemView.findViewById(R.id.cv_rest_add_cart);
            etRestQuan = (EditText) itemView.findViewById(R.id.restQuantity);
            rvDec = (RelativeLayout) itemView.findViewById(R.id.restDecrement);
            rvInc = (RelativeLayout) itemView.findViewById(R.id.restIncrement);
            tvCart = (TextView) itemView.findViewById(R.id.tv_add_to_cart);
            ivFav = (ImageView) itemView.findViewById(R.id.restFav);

            cvAddCart.setOnClickListener(this);
            rvInc.setOnClickListener(this);
            rvDec.setOnClickListener(this);
            ivFav.setOnClickListener(this);
            cvProducts.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int position = getAdapterPosition();
            //Click on product
            if (view.getId() == R.id.cvProducts) {
                Intent intent = new Intent(c, ItemDisplayActivity.class);
                intent.putExtra("Name", products.get(position).getProductName());
                intent.putExtra("Price", products.get(position).getPrice());
                intent.putExtra("Features", products.get(position).getFeatures());
                intent.putExtra("Image", products.get(position).getProductImage());
                intent.putExtra("ID", products.get(position).getId());
                c.startActivity(intent);
            }

            //Click on add to cart
            else if (view.getId() == R.id.cv_rest_add_cart) {
                //Add to cart
                Double val = Double.parseDouble(etRestQuan.getText().toString());
                int proId = products.get(position).getId();
                Long mob = Long.parseLong(mobileNumber);
                addCartItem(mob, val, proId);

            } else if (view.getId() == R.id.restIncrement) {

                if (restQuantity[position] < 10) {
                    restQuantity[position]++;
                    etRestQuan.setText(String.valueOf(restQuantity[position]));
                }

            } else if (view.getId() == R.id.restDecrement) {

                if (restQuantity[position] > 1) {
                    restQuantity[position]--;
                    etRestQuan.setText(String.valueOf(restQuantity[position]));
                }
            } else if (view.getId() == R.id.restFav) {
                //Add to favorites
                productFav(products.get(position).getId(), "1");
            }


        }

        private void productFav(Integer productID, final String option) {
            //option is 0 or 1 -
            //1 for adding , 0 for removing

            String url = "http://ec2-34-208-181-152.us-west-2.compute.amazonaws.com/users/favs/" + mobileNumber + "/" + option + "/";
            JSONObject obj = new JSONObject();
            try {
                obj.put("LastItem", productID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Volley.newRequestQueue(c).add(new JsonObjectRequest(Request.Method.PATCH, url, obj, new com.android.volley.Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (option.equals("1")) {
                        Picasso.with(c).load(R.drawable.fav_filled_48).into(ivFav);
                        Toast.makeText(c, "Added to favorites", Toast.LENGTH_SHORT).show();
                    } else {
                    }

                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(c, "Network error", Toast.LENGTH_SHORT).show();
                }
            }));
        }


        void addCartItem(Long mobile, Double val, int productID) {

            CartItemPost item = new CartItemPost(mobile, val, productID, null);

            Call<CartItemPost> call = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(djangoBaseUrl).build().create(DataInterface.class).putCartItemOnServer(item);
            call.enqueue(new Callback<CartItemPost>() {
                @Override
                public void onResponse(Call<CartItemPost> call, Response<CartItemPost> response) {
                    tvCart.setText("Added to cart");

                    Call<List<CartItem>> callItems = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(djangoBaseUrl).build().create(DataInterface.class)
                            .getUserCartItems(mobileNumber);
                    callItems.enqueue(new Callback<List<CartItem>>() {
                        @Override
                        public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                            List<CartItem> items = response.body();
                            //Log.d("items", String.valueOf(items));

                            if (items != null && items.size() > 0) {

                                //Accesss views?
//                                cartItems.setVisibility(View.VISIBLE);
//                                itemCount.setText(String.valueOf(items.size()));

                            } else {

//                                cartItems.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<CartItem>> call, Throwable t) {

                        }
                    });


                }

                @Override
                public void onFailure(Call<CartItemPost> call, Throwable t) {
                    Toast.makeText(c, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }


            });
        }
    }


}