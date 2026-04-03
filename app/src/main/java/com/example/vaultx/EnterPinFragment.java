package com.example.vaultx;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;

public class EnterPinFragment extends Fragment {

    private StringBuilder pin = new StringBuilder();
    private View[] dots = new View[4];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_enter_pin, container, false);

        LinearLayout dotLayout = view.findViewById(R.id.pinDots);

        for (int i = 0; i < 4; i++) {
            dots[i] = dotLayout.getChildAt(i);
        }

        // Number buttons
        GridLayout grid = view.findViewById(R.id.keypadGrid);

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

        return view;
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

    private void deleteDigit() {
        if (pin.length() > 0) {
            pin.deleteCharAt(pin.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < 4; i++) {
            if (i < pin.length()) {
                dots[i].setBackgroundResource(R.drawable.dot_active);
            } else {
                dots[i].setBackgroundResource(R.drawable.dot_inactive);
            }
        }
    }

    private void checkPin() {
        String savedPin = PinManager.getPin(getContext());

        if (pin.toString().equals(savedPin)) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            requireActivity().finish();
        } else {
            shakeAnimation();
            pin.setLength(0);
            updateDots();
        }
    }

    private void shakeAnimation() {
        View root = getView();
        if (root != null) {
            ObjectAnimator shake = ObjectAnimator.ofFloat(root, "translationX",
                    0, 20, -20, 20, -20, 0);
            shake.setDuration(400);
            shake.start();
        }
    }
}