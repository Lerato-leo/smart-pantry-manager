package za.ac.richfield.dijong.data;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.dijong.data.dao.RecipeDao;
import za.ac.richfield.dijong.data.entity.Recipe;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;

/**
 * The built-in cookbook: 20 South African dishes, each for about four people, with their
 * ingredients in the units they're bought and measured in here, and the method as numbered
 * steps.
 *
 * <p>A few rules keep the data useful for strict matching:
 * <ul>
 *   <li>The same ingredient has the same name in every recipe ("onion", "flour", "curry
 *   powder"), so one pantry item serves them all.</li>
 *   <li>Tap water is never listed as an ingredient; the steps say how much to boil.</li>
 *   <li>Every ingredient a step uses is in that recipe's list.</li>
 * </ul>
 *
 * <p>Bump {@link #SEED_VERSION} whenever the recipes change: {@link DijongDatabase} reloads
 * them on the next launch, so existing installs get the new versions without a reinstall.
 */
final class SouthAfricanRecipeSeeder {

    /** Raise this after editing the recipes below so installed copies pick up the changes. */
    static final int SEED_VERSION = 2;

    /** How many recipes {@link #populateRecipes} adds; the brief asks for 15 to 20. */
    static final int RECIPE_COUNT = 20;

    private SouthAfricanRecipeSeeder() {
    }

    /**
     * Replaces whatever recipes are stored with the built-in set, in one transaction so the
     * app never sees a half-loaded cookbook. Pantry items are never touched.
     */
    static void replaceAllRecipes(DijongDatabase database) {
        RecipeDao recipeDao = database.recipeDao();
        database.runInTransaction(() -> {
            recipeDao.deleteAllIngredients();
            recipeDao.deleteAllRecipes();
            populateRecipes(recipeDao);
        });
    }

    /**
     * Makes sure the full, current cookbook is in the database, reloading it if recipes are
     * missing (a first launch, or a launch interrupted mid-seed) or were loaded from an older
     * {@link #SEED_VERSION}. Safe to call on every launch: when everything is in place it's a
     * single count query.
     *
     * @param loadedVersion the seed version recorded after the last successful load, or 0
     * @return true if the recipes were (re)loaded
     */
    static boolean ensureRecipesLoaded(DijongDatabase database, int loadedVersion) {
        boolean upToDate = loadedVersion == SEED_VERSION
                && database.recipeDao().countRecipes() == RECIPE_COUNT;
        if (upToDate) {
            return false;
        }
        replaceAllRecipes(database);
        return true;
    }

    static void populateRecipes(RecipeDao recipeDao) {
        addRecipe(recipeDao, "Pap and Chakalaka",
                "1. Bring 1.25 L of salted water to the boil and slowly stir in the maize meal. "
                        + "2. Cover and let the pap steam on low heat for 30 minutes, stirring now and then. "
                        + "3. Meanwhile fry the onion, green pepper and grated carrot in the oil until soft. "
                        + "4. Stir in the curry powder, then the baked beans, and simmer for 10 minutes. "
                        + "5. Serve the chakalaka spooned over the pap.",
                new Object[]{"maize meal", 500, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"green pepper", 1, "unit"},
                new Object[]{"carrot", 2, "unit"},
                new Object[]{"baked beans", 1, "can"},
                new Object[]{"curry powder", 1, "tbsp"},
                new Object[]{"vegetable oil", 2, "tbsp"});

        addRecipe(recipeDao, "Boerewors Rolls",
                "1. Braai the boerewors over medium coals for about 15 minutes, turning it now and then. "
                        + "2. Fry the sliced onions in the oil until golden. "
                        + "3. Add the chopped tomatoes and cook down into a thick relish. "
                        + "4. Split the rolls, add a piece of boerewors to each and top with the relish.",
                new Object[]{"boerewors", 500, "g"},
                new Object[]{"bread roll", 4, "unit"},
                new Object[]{"onion", 2, "unit"},
                new Object[]{"tomato", 2, "unit"},
                new Object[]{"vegetable oil", 1, "tbsp"});

        addRecipe(recipeDao, "Bobotie",
                "1. Soak the bread in half the milk. "
                        + "2. Fry the onion and garlic in the oil, then brown the mince with the curry powder. "
                        + "3. Squeeze out the bread and mix it into the mince with the chutney and apricot jam. "
                        + "4. Spread into a baking dish and pour over the eggs beaten with the rest of the milk. "
                        + "5. Bake at 180 °C for 35 minutes, until the topping is set and golden.",
                new Object[]{"beef mince", 500, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"garlic", 2, "clove"},
                new Object[]{"curry powder", 1, "tbsp"},
                new Object[]{"bread", 2, "slice"},
                new Object[]{"milk", 250, "ml"},
                new Object[]{"egg", 2, "unit"},
                new Object[]{"chutney", 2, "tbsp"},
                new Object[]{"apricot jam", 1, "tbsp"},
                new Object[]{"vegetable oil", 1, "tbsp"});

        addRecipe(recipeDao, "Melktert",
                "1. Line a pie dish with the pastry and blind bake at 200 °C for 15 minutes. "
                        + "2. Heat the milk and butter until just boiling. "
                        + "3. Whisk the eggs, sugar and flour together, then slowly whisk in the hot milk. "
                        + "4. Return to low heat and stir until the custard thickens. "
                        + "5. Pour into the pastry case, dust with cinnamon and chill until set.",
                new Object[]{"milk", 1, "L"},
                new Object[]{"egg", 3, "unit"},
                new Object[]{"sugar", 150, "g"},
                new Object[]{"flour", 3, "tbsp"},
                new Object[]{"butter", 1, "tbsp"},
                new Object[]{"shortcrust pastry", 400, "g"},
                new Object[]{"cinnamon", 1, "tsp"});

        addRecipe(recipeDao, "Koeksisters",
                "1. Boil the sugar with 500 ml of water, the lemon juice and ginger for 5 minutes, then chill the syrup overnight. "
                        + "2. Rub the butter into the flour and baking powder, then mix in the egg and milk to make a soft dough. "
                        + "3. Roll out, cut into strips and plait them in threes. "
                        + "4. Deep-fry in hot oil until golden brown. "
                        + "5. Dip straight into the ice-cold syrup, then drain on a rack.",
                new Object[]{"flour", 500, "g"},
                new Object[]{"baking powder", 2, "tsp"},
                new Object[]{"butter", 50, "g"},
                new Object[]{"egg", 1, "unit"},
                new Object[]{"milk", 250, "ml"},
                new Object[]{"sugar", 1, "kg"},
                new Object[]{"lemon juice", 2, "tbsp"},
                new Object[]{"ginger", 1, "tsp"},
                new Object[]{"vegetable oil", 750, "ml"});

        addRecipe(recipeDao, "Vetkoek with Curried Mince",
                "1. Mix the flour, yeast and sugar with about 300 ml of lukewarm water into a soft dough and leave to rise for an hour. "
                        + "2. Fry the onion, then brown the mince with the curry powder. "
                        + "3. Add the chopped tomatoes and simmer until thick. "
                        + "4. Deep-fry balls of dough in hot oil until puffed and golden. "
                        + "5. Split the vetkoek and fill with the curried mince.",
                new Object[]{"flour", 500, "g"},
                new Object[]{"yeast", 10, "g"},
                new Object[]{"sugar", 1, "tbsp"},
                new Object[]{"vegetable oil", 750, "ml"},
                new Object[]{"beef mince", 500, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"tomato", 2, "unit"},
                new Object[]{"curry powder", 1, "tbsp"});

        addRecipe(recipeDao, "Malva Pudding",
                "1. Beat the egg with 200 g of the sugar, then beat in the apricot jam and 1 tbsp of the butter. "
                        + "2. Stir the bicarbonate of soda and vinegar into the milk, then fold into the batter with the flour. "
                        + "3. Bake in a greased dish at 180 °C for 40 minutes. "
                        + "4. Melt the rest of the butter and sugar with the cream and 125 ml of hot water. "
                        + "5. Pour the sauce over the pudding as soon as it comes out of the oven.",
                new Object[]{"flour", 150, "g"},
                new Object[]{"sugar", 300, "g"},
                new Object[]{"egg", 1, "unit"},
                new Object[]{"apricot jam", 1, "tbsp"},
                new Object[]{"butter", 125, "g"},
                new Object[]{"milk", 250, "ml"},
                new Object[]{"cream", 250, "ml"},
                new Object[]{"bicarbonate of soda", 1, "tsp"},
                new Object[]{"vinegar", 1, "tsp"});

        addRecipe(recipeDao, "Bunny Chow",
                "1. Fry the onion, garlic and curry powder in the oil until fragrant. "
                        + "2. Add the chicken and brown it, then the diced potatoes and chopped tomatoes. "
                        + "3. Simmer with a splash of water until the chicken is tender and the sauce is thick. "
                        + "4. Cut the loaf into quarters and hollow out each one. "
                        + "5. Fill the bread with curry and serve with the bread you scooped out.",
                new Object[]{"bread loaf", 1, "unit"},
                new Object[]{"chicken", 500, "g"},
                new Object[]{"potato", 2, "unit"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"tomato", 2, "unit"},
                new Object[]{"garlic", 2, "clove"},
                new Object[]{"curry powder", 2, "tbsp"},
                new Object[]{"vegetable oil", 2, "tbsp"});

        addRecipe(recipeDao, "Potjiekos",
                "1. Heat the oil in the potjie over the coals and brown the beef in batches. "
                        + "2. Add the onions and fry until soft. "
                        + "3. Pour in the stock, cover and simmer gently for 1.5 hours. "
                        + "4. Layer the carrots, potatoes and butternut on top without stirring. "
                        + "5. Cover and simmer for another hour until the vegetables are tender, then serve with pap or rice.",
                new Object[]{"beef", 1, "kg"},
                new Object[]{"potato", 4, "unit"},
                new Object[]{"carrot", 3, "unit"},
                new Object[]{"butternut", 500, "g"},
                new Object[]{"onion", 2, "unit"},
                new Object[]{"beef stock", 500, "ml"},
                new Object[]{"vegetable oil", 2, "tbsp"});

        addRecipe(recipeDao, "Sosaties",
                "1. Fry the sliced onions until soft, then add the curry powder, apricot jam and vinegar and simmer for 5 minutes. "
                        + "2. Cool the marinade and pour it over the cubed lamb. "
                        + "3. Leave in the fridge overnight. "
                        + "4. Thread the lamb, onion and dried apricots onto skewers. "
                        + "5. Braai over hot coals, basting with the marinade, until done to your liking.",
                new Object[]{"lamb", 1, "kg"},
                new Object[]{"onion", 2, "unit"},
                new Object[]{"dried apricots", 200, "g"},
                new Object[]{"curry powder", 2, "tbsp"},
                new Object[]{"apricot jam", 3, "tbsp"},
                new Object[]{"vinegar", 125, "ml"});

        addRecipe(recipeDao, "Umngqusho (Samp and Beans)",
                "1. Soak the samp and sugar beans in plenty of water overnight, then drain. "
                        + "2. Cover with fresh water and simmer for 2 to 3 hours until soft, topping up as needed. "
                        + "3. Fry the onion in the oil, add the chopped tomatoes and cook down. "
                        + "4. Stir the tomato and onion through the samp and beans, season and serve hot.",
                new Object[]{"samp", 500, "g"},
                new Object[]{"sugar beans", 250, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"tomato", 2, "unit"},
                new Object[]{"vegetable oil", 2, "tbsp"});

        addRecipe(recipeDao, "Chakalaka",
                "1. Fry the onion and garlic in the oil until soft. "
                        + "2. Add the green pepper and grated carrots and cook for 5 minutes. "
                        + "3. Stir in the curry powder, then the chopped tomatoes. "
                        + "4. Add the baked beans and simmer for 10 minutes until thick. "
                        + "5. Serve warm or cold, with pap or alongside a braai.",
                new Object[]{"onion", 1, "unit"},
                new Object[]{"garlic", 2, "clove"},
                new Object[]{"green pepper", 1, "unit"},
                new Object[]{"carrot", 3, "unit"},
                new Object[]{"tomato", 2, "unit"},
                new Object[]{"baked beans", 1, "can"},
                new Object[]{"curry powder", 2, "tsp"},
                new Object[]{"vegetable oil", 2, "tbsp"});

        addRecipe(recipeDao, "Biltong and Cheese Board",
                "1. Slice the biltong thinly against the grain. "
                        + "2. Cut the cheddar into cubes. "
                        + "3. Arrange the biltong, cheese and crackers on a board. "
                        + "4. Serve with a small bowl of chutney.",
                new Object[]{"biltong", 200, "g"},
                new Object[]{"cheddar cheese", 200, "g"},
                new Object[]{"crackers", 200, "g"},
                new Object[]{"chutney", 3, "tbsp"});

        addRecipe(recipeDao, "Ouma's Rusks",
                "1. Mix the flour, sugar and baking powder, then rub in the butter. "
                        + "2. Beat the eggs into the buttermilk and mix into the flour to form a dough. "
                        + "3. Shape into balls, pack tightly into greased loaf tins and bake at 180 °C for 1 hour. "
                        + "4. Cool, break into rusks and dry out overnight in a 100 °C oven.",
                new Object[]{"flour", 1, "kg"},
                new Object[]{"sugar", 250, "g"},
                new Object[]{"baking powder", 4, "tsp"},
                new Object[]{"butter", 500, "g"},
                new Object[]{"egg", 2, "unit"},
                new Object[]{"buttermilk", 500, "ml"});

        addRecipe(recipeDao, "Rooibos Iced Tea",
                "1. Pour 1 L of boiling water over the rooibos tea bags and steep for 5 minutes. "
                        + "2. Remove the tea bags and stir in the honey while the tea is hot. "
                        + "3. Add the lemon juice and leave to cool. "
                        + "4. Chill and serve over ice.",
                new Object[]{"rooibos tea bag", 4, "unit"},
                new Object[]{"honey", 3, "tbsp"},
                new Object[]{"lemon juice", 2, "tbsp"});

        addRecipe(recipeDao, "Frikkadels",
                "1. Soak the bread in the milk, then squeeze it out. "
                        + "2. Mix the bread into the mince with the grated onion, garlic and egg, and season. "
                        + "3. Shape into balls and flatten them slightly. "
                        + "4. Fry in the oil for about 5 minutes a side, until browned and cooked through.",
                new Object[]{"beef mince", 500, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"garlic", 1, "clove"},
                new Object[]{"egg", 1, "unit"},
                new Object[]{"bread", 2, "slice"},
                new Object[]{"milk", 125, "ml"},
                new Object[]{"vegetable oil", 3, "tbsp"});

        addRecipe(recipeDao, "Peppermint Crisp Tart",
                "1. Whip the cream until it holds soft peaks. "
                        + "2. Beat the caramel treat until smooth and fold it into the cream. "
                        + "3. Layer the tennis biscuits and caramel cream in a dish, finishing with cream. "
                        + "4. Grate the Peppermint Crisp over the top. "
                        + "5. Chill for at least four hours before serving.",
                new Object[]{"tennis biscuits", 200, "g"},
                new Object[]{"caramel treat", 1, "can"},
                new Object[]{"cream", 500, "ml"},
                new Object[]{"peppermint crisp", 150, "g"});

        addRecipe(recipeDao, "Pampoenkoekies (Pumpkin Fritters)",
                "1. Mash the cooked pumpkin and beat in the eggs. "
                        + "2. Mix in the flour and baking powder to make a thick batter. "
                        + "3. Fry spoonfuls in hot oil until golden on both sides. "
                        + "4. Drain and sprinkle with the sugar mixed with the cinnamon.",
                new Object[]{"pumpkin", 500, "g"},
                new Object[]{"egg", 2, "unit"},
                new Object[]{"flour", 125, "g"},
                new Object[]{"baking powder", 2, "tsp"},
                new Object[]{"sugar", 3, "tbsp"},
                new Object[]{"cinnamon", 2, "tsp"},
                new Object[]{"vegetable oil", 250, "ml"});

        addRecipe(recipeDao, "Braaibroodjies",
                "1. Butter the bread on one side of each slice. "
                        + "2. Spread chutney on the unbuttered side of half the slices. "
                        + "3. Top with cheese, sliced tomato and onion, and close with the rest, buttered side out. "
                        + "4. Braai in a hinged grid over gentle coals until golden and the cheese has melted.",
                new Object[]{"bread", 8, "slice"},
                new Object[]{"cheddar cheese", 200, "g"},
                new Object[]{"tomato", 2, "unit"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"butter", 4, "tbsp"},
                new Object[]{"chutney", 4, "tbsp"});

        addRecipe(recipeDao, "Pampoensop (Butternut Soup)",
                "1. Melt the butter and fry the onion until soft, then add the curry powder. "
                        + "2. Add the cubed butternut and potato and the stock. "
                        + "3. Simmer for 25 minutes until the vegetables are soft. "
                        + "4. Blend until smooth, stir in the cream and season before serving.",
                new Object[]{"butternut", 1, "kg"},
                new Object[]{"potato", 1, "unit"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"vegetable stock", 1, "L"},
                new Object[]{"cream", 125, "ml"},
                new Object[]{"curry powder", 1, "tsp"},
                new Object[]{"butter", 1, "tbsp"});
    }

    private static void addRecipe(RecipeDao recipeDao, String name, String prepSteps, Object[]... ingredients) {
        Recipe recipe = new Recipe(0, name, prepSteps);
        long recipeId = recipeDao.insertRecipe(recipe);

        List<RecipeIngredient> recipeIngredients = new ArrayList<>();
        for (Object[] ingredient : ingredients) {
            String ingredientName = (String) ingredient[0];
            double quantity = ((Number) ingredient[1]).doubleValue();
            String unit = (String) ingredient[2];
            recipeIngredients.add(new RecipeIngredient(0, recipeId, ingredientName, quantity, unit));
        }
        recipeDao.insertIngredients(recipeIngredients);
    }
}
