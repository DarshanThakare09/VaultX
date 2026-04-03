package com.example.vaultx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class SetPinFragment extends Fragment {

    EditText etPin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_set_pin, container, false);

        etPin = view.findViewById(R.id.etPin);

        view.findViewById(R.id.btnNext).setOnClickListener(v -> {
            String pin = etPin.getText().toString();

            if (pin.length() != 4) {
                Toast.makeText(getContext(), "Enter 4 digit PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("pin", pin);

            ConfirmPinFragment fragment = new ConfirmPinFragment();
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        });

        return view;
    }
}