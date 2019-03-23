package com.adampolt.metrocard;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PurchaseService {
    @GET("/bestpurchase")
    Call<PurchaseResponse> getBestPurchase(@Query("balance") int currentBalance);
}
