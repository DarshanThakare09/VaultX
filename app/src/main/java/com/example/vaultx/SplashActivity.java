package com.example.vaultx;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.animation.ValueAnimator;
import android.animation.ObjectAnimator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    // ── Tuning ────────────────────────────────────────────────────────────────

    // Radius of the imaginary circle. Larger = flatter arc.
    private static final float CIRCLE_RADIUS = 700f;

    private static final float CARD_GAP = 30f;


    private static final float[] INITIAL_ANGLES = {
            180f,   // ghost 1  (far right, hidden below screen)
            210f,   // ghost 2  (right, partially hidden)
            240f,   // card  1  (left  visible)
            270f,   // card  2  (center, upright)
            300f,   // card  3  (right visible)
            330f,   // ghost 3  (entering from far right)
    };

    // Clockwise rotation speed (degrees per second).
    private static final float DEG_PER_SECOND = 18f;


    // ── State ─────────────────────────────────────────────────────────────────
    private float circleCx, circleCy;
    private View[] cards; // index matches INITIAL_ANGLES
    private FrameLayout container;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        container = findViewById(R.id.cardContainer);


        cards = new View[]{
                findViewById(R.id.card1),   // index 0 — ghost 1
                findViewById(R.id.card2),   // index 1 — ghost 2
                findViewById(R.id.card3),   // index 2 — card  1 (Convert)
                findViewById(R.id.card4),   // index 3 — card  2 (Scan)
                findViewById(R.id.card5),   // index 4 — card  3 (Edit)
                findViewById(R.id.card6),   // index 5 — ghost 3
        };

        // Hide all cards initially
        for (View c : cards) {
            c.setAlpha(0f);
        }

        container.post(() -> {
            circleCx = container.getWidth()  / 2f;
            // Push circle center further below so the arc peak (cards) sits
            // in the upper half of the container — well clear of the search button.
            circleCy = container.getHeight() + CIRCLE_RADIUS * 0.72f;

            // Place all cards at their starting positions
            placeAllCards(0f);

            int[] order = {3, 2, 4, 1, 0, 5};
            int delay = 0;

            for (int i : order) {
                fadeIn(cards[i], delay);
                delay += 130;
            }
            // Start infinite clockwise rotation
            new Handler().postDelayed(this::startInfiniteRotation, 600);
        });

        // ── Typing text ───────────────────────────────────────────────────────
        TextView tv   = findViewById(R.id.tvTyping);
        String   text = "Secure.\nPrivate.\nYours.";
        Handler  h    = new Handler();
        for (int i = 0; i <= text.length(); i++) {
            final int idx = i;
            h.postDelayed(() -> tv.setText(text.substring(0, idx)), i *80L);
        }

        // ── Navigate after 3 s ────────────────────────────────────────────────
        new Handler().postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                startActivity(new Intent(this, PinActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
            finish();
        }, 3300);
    }

    // ── Infinite rotation ─────────────────────────────────────────────────────


    private void startInfiniteRotation() {
        long stepDuration = (long) (CARD_GAP / DEG_PER_SECOND * 1000);

        ValueAnimator anim = ValueAnimator.ofFloat(0f, 360f);
        anim.setDuration((long) (360f / DEG_PER_SECOND * 1000));
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setInterpolator(new DecelerateInterpolator(0.5f));

        anim.addUpdateListener(animation -> {
            float angle = (float) animation.getAnimatedValue();
            placeAllCards(angle);
        });

        anim.start();
    }

    // ── Arc math ──────────────────────────────────────────────────────────────

    private void placeAllCards(float rotationOffset) {
        for (int i = 0; i < cards.length; i++) {
            placeCard(cards[i], INITIAL_ANGLES[i] + rotationOffset);
        }
    }


    private void placeCard(View view, float angle) {
        // Keep angle in [0, 360) to avoid float drift
        angle = ((angle % 360f) + 360f) % 360f;

        double rad = Math.toRadians(angle);

        float cardCx = (float) (circleCx + CIRCLE_RADIUS * Math.cos(rad));
        float cardCy = (float) (circleCy + CIRCLE_RADIUS * Math.sin(rad));

        view.setX(cardCx - view.getWidth()  / 2f);
        view.setY(cardCy - view.getHeight() / 2f);

        // Rotate card to point outward from circle center (tangent to arc)
        view.setRotation(angle - 270f);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void fadeIn(View view, long delayMs) {
        ObjectAnimator a = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        a.setDuration(500);
        a.setStartDelay(delayMs);
        a.start();
    }
}