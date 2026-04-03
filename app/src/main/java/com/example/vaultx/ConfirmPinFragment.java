package  com.example.vaultx;
import static androidx.core.content.ContextCompat.startActivity;
import static java.security.AccessController.getContext;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.vaultx.PinManager;

public class ConfirmPinFragment extends Fragment {

    private String originalPin;
    private StringBuilder pin = new StringBuilder();
    private View[] dots = new View[4];

    public static ConfirmPinFragment newInstance(String pin) {
        ConfirmPinFragment fragment = new ConfirmPinFragment();
        Bundle args = new Bundle();
        args.putString("pin", pin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_confirm_pin, container, false);


        originalPin = getArguments().getString("pin");

        View mainLayout = view.findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply top padding equal to status bar height
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );

            return insets;
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goBackToSetPin();
                    }
                }
        );
        CardView btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> goBackToSetPin());

        LinearLayout dotLayout = view.findViewById(R.id.pinDots);
        GridLayout grid = view.findViewById(R.id.keypadGrid);

        for (int i = 0; i < 4; i++) {
            dots[i] = dotLayout.getChildAt(i);
        }

        setupKeypad(grid);

        return view;
    }

    private void setupKeypad(GridLayout grid) {
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);

            if (child instanceof Button) {
                Button btn = (Button) child;

                if (btn.getId() == R.id.btnDelete) {
                    btn.setOnClickListener(v -> deleteDigit());
                } else {
                    btn.setOnClickListener(v -> addDigit(btn.getText().toString()));
                }
            }
        }
    }

    private void addDigit(String digit) {
        if (pin.length() < 4) {
            pin.append(digit);
            updateDots();

            if (pin.length() == 4) {
                checkPin();
            }
        }
    }

    private void checkPin() {
        if (pin.toString().equals(originalPin)) {

            PinManager.savePin(getContext(), originalPin);

            startActivity(new Intent(getActivity(), MainActivity.class));
            requireActivity().finish();

        } else {
            shakeAnimation();
            pin.setLength(0);
            updateDots();
        }
    }

    private void deleteDigit() {
        if (pin.length() > 0) {
            pin.deleteCharAt(pin.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < 4; i++) {
            dots[i].setBackgroundResource(
                    i < pin.length() ? R.drawable.dot_active : R.drawable.dot_inactive
            );
        }
    }

    private void shakeAnimation() {
        View root = getView();
        if (root != null) {
            ObjectAnimator shake = ObjectAnimator.ofFloat(root, "translationX",
                    0, 25, -25, 25, -25, 0);
            shake.setDuration(400);
            shake.start();
        }
    }
    private void goBackToSetPin() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                )
                .replace(R.id.container, new SetPinFragment())
                .commit();
    }
}