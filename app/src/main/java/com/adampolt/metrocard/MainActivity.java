package com.adampolt.metrocard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Build our Retrofit object which will create Services
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://metrocard.polt.me")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create a Purchase service to make network calls
        final PurchaseService service = retrofit.create(PurchaseService.class);

        // Get references to the Views that we'll update
        final EditText balanceTextView = findViewById(R.id.balance);
        final TextView resultTextView = findViewById(R.id.results);
        Button submitButton = findViewById(R.id.submit);

        // When the Submit button gets clicked
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the content of the Balance TextView as a String
                String balanceString = balanceTextView.getText().toString();

                // Make sure the String is not empty and numeric
                if (balanceString.length() > 0 && TextUtils.isDigitsOnly(balanceString)) {
                    // Parse the String to an int so we can pass it to the service
                    int currentBalance = Integer.parseInt(balanceString);

                    // Create a network call to get the best purchase for the parsed balance
                    Call<PurchaseResponse> call = service.getBestPurchase(currentBalance);

                    // Enqueue the call on a background thread
                    call.enqueue(new Callback<PurchaseResponse>() {
                        @Override
                        public void onResponse(Call<PurchaseResponse> call, Response<PurchaseResponse> response) {
                            // The call succeeded, but we still need to check if we got valid data!
                            if (response.body() != null && response.body().data != null) {
                                // Get the suggested purchase price in cents from the response
                                int bestPurchase = response.body().data;

                                // Format the string as dollars and cents
                                String formattedValue = formatCents(bestPurchase);
                                // Show the formatted results in the result textview
                                resultTextView.setText(getString(R.string.best_purchase_format, formattedValue));
                            } else {
                                // The call succeeded but the data was missing or invalid
                                resultTextView.setText(R.string.network_error);
                            }
                        }

                        @Override
                        public void onFailure(Call<PurchaseResponse> call, Throwable t) {
                            // The call failed -- let the user know
                            resultTextView.setText(R.string.network_error);
                        }
                    });
                } else {
                    // There was a problem with the user's input -- let them know
                    resultTextView.setText(R.string.input_error);
                }
            }
        });
    }

    // Takes an amount in cents and formats it as dollars and cents
    private String formatCents(int cents) {
        // Divide cents by 100 to get dollars
        double dollars = cents / 100.0;

        // Get a Numberformat that formats a double value as dollars and cents
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        return currencyFormat.format(dollars);
    }
}
