package  com.example.vaultx;
import static androidx.core.content.ContextCompat.startActivity;
import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.vaultx.PinManager;

public class ConfirmPinFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_confirm_pin, container, false);

        String originalPin = getArguments().getString("pin");

        EditText etConfirm = view.findViewById(R.id.etConfirmPin);

        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {

            String confirmPin = etConfirm.getText().toString();

            if (!confirmPin.equals(originalPin)) {
                Toast.makeText(getContext(), "PIN not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save PIN
            PinManager.savePin(getContext(), confirmPin);

            // Go to Main
            startActivity(new Intent(getActivity(), MainActivity.class));
            requireActivity().finish();
        });

        return view;
    }
}