package com.bloom.app.data.model

// ─────────────────────────────────────────────────────────────────────────────
// GardenStage
//
// The garden is Bloom's emotional signature.
// Growth should feel earned — not gamified.
// Each stage represents real reflection consistency.
//
// Stage progression:
//   0 entries  → Seed    (just planted something)
//   1-3        → Sprout  (something is growing)
//   4-9        → Leaf    (taking real shape)
//   10-19      → Flower  (blooming consistently)
//   20+        → Tree    (deeply rooted habit)
// ─────────────────────────────────────────────────────────────────────────────

enum class GardenStage(
    val displayName: String,
    val description: String,
    val minEntries: Int,
) {
    SEED(
        displayName  = "Seed",
        description  = "You've planted something beautiful.",
        minEntries   = 0,
    ),
    SPROUT(
        displayName  = "Sprout",
        description  = "Something is beginning to grow.",
        minEntries   = 1,
    ),
    LEAF(
        displayName  = "Leaf",
        description  = "Your reflection is taking shape.",
        minEntries   = 4,
    ),
    FLOWER(
        displayName  = "Flower",
        description  = "You're blooming with consistency.",
        minEntries   = 10,
    ),
    TREE(
        displayName  = "Tree",
        description  = "You've built something that lasts.",
        minEntries   = 20,
    );

    companion object {
        /**
         * Determines the current garden stage based on total journal entry count.
         * Returns the highest stage whose threshold has been met.
         */
        fun fromEntryCount(count: Int): GardenStage =
            entries.lastOrNull { count >= it.minEntries } ?: SEED
    }
}
