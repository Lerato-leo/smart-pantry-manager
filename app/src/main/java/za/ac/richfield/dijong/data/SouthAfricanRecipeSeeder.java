package za.ac.richfield.dijong.data;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.dijong.data.dao.RecipeDao;
import za.ac.richfield.dijong.data.entity.Recipe;
import za.ac.richfield.dijong.data.entity.RecipeIngredient;

/**
 * Fills a freshly created database with a starter cookbook of South African dishes, so a new
 * install has something to match against before the person has added any of their own stock.
 */
final class SouthAfricanRecipeSeeder {

    private SouthAfricanRecipeSeeder() {
    }

    static void populateRecipes(RecipeDao recipeDao) {
        addRecipe(recipeDao, "Maize Porridge with Spicy Bean Relish",
                "1. Bring the water to the boil and stir in the maize meal to make a stiff porridge. "
                        + "2. Fry the onion and green pepper until soft. "
                        + "3. Stir in the baked beans and curry powder and simmer for 10 minutes. "
                        + "4. Serve the relish spooned over the porridge.",
                new Object[]{"maize meal", 250, "g"},
                new Object[]{"water", 750, "ml"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"green pepper", 1, "unit"},
                new Object[]{"baked beans", 1, "can"},
                new Object[]{"curry powder", 1, "tbsp"});

        addRecipe(recipeDao, "Farmer's Sausage Rolls",
                "1. Grill the sausage over hot coals until cooked through. "
                        + "2. Fry the onions until soft and caramelised. "
                        + "3. Split the rolls and tuck in the sausage. "
                        + "4. Top with the fried onions and tomato relish.",
                new Object[]{"sausage", 500, "g"},
                new Object[]{"bread roll", 4, "unit"},
                new Object[]{"onion", 2, "unit"},
                new Object[]{"tomato relish", 100, "ml"});

        addRecipe(recipeDao, "Spiced Mince Bake",
                "1. Fry the onion and garlic, then brown the mince with curry powder. "
                        + "2. Stir in the chutney and the bread soaked in milk. "
                        + "3. Spoon into a dish and pour over an egg-and-milk custard. "
                        + "4. Bake until the topping sets golden brown.",
                new Object[]{"beef mince", 500, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"garlic", 2, "clove"},
                new Object[]{"curry powder", 1, "tbsp"},
                new Object[]{"bread", 2, "slice"},
                new Object[]{"milk", 250, "ml"},
                new Object[]{"egg", 2, "unit"},
                new Object[]{"chutney", 2, "tbsp"});

        addRecipe(recipeDao, "Milk Tart",
                "1. Line a dish with the pastry and blind bake. "
                        + "2. Heat the milk with a stick of cinnamon. "
                        + "3. Whisk in the eggs, sugar and flour, then thicken over low heat. "
                        + "4. Pour into the pastry case and chill before dusting with cinnamon.",
                new Object[]{"milk", 500, "ml"},
                new Object[]{"egg", 3, "unit"},
                new Object[]{"sugar", 100, "g"},
                new Object[]{"flour", 30, "g"},
                new Object[]{"shortcrust pastry", 1, "unit"},
                new Object[]{"cinnamon", 1, "tsp"});

        addRecipe(recipeDao, "Plaited Syrup Doughnuts",
                "1. Make a soft dough and plait small pieces. "
                        + "2. Deep-fry the plaits until golden brown. "
                        + "3. Dip immediately into ice-cold sugar syrup. "
                        + "4. Drain on a rack and serve chilled.",
                new Object[]{"flour", 250, "g"},
                new Object[]{"sugar", 400, "g"},
                new Object[]{"water", 250, "ml"},
                new Object[]{"yeast", 1, "tsp"},
                new Object[]{"ginger", 1, "tsp"});

        addRecipe(recipeDao, "Fried Dough Buns with Curried Mince",
                "1. Make a yeast dough and leave it to rise until doubled. "
                        + "2. Deep-fry spoonfuls of dough until puffed and golden. "
                        + "3. Brown the mince with the onion and curry powder. "
                        + "4. Split the buns and spoon in the mince.",
                new Object[]{"flour", 300, "g"},
                new Object[]{"yeast", 1, "tsp"},
                new Object[]{"beef mince", 300, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"curry powder", 1, "tbsp"});

        addRecipe(recipeDao, "Baked Apricot Sponge Pudding",
                "1. Cream the sugar and egg, then mix in the apricot jam and flour. "
                        + "2. Bake in a greased dish until springy to the touch. "
                        + "3. Warm the cream, butter and sugar together into a sauce. "
                        + "4. Pour the hot sauce over the pudding as soon as it leaves the oven.",
                new Object[]{"flour", 200, "g"},
                new Object[]{"sugar", 200, "g"},
                new Object[]{"egg", 1, "unit"},
                new Object[]{"apricot jam", 2, "tbsp"},
                new Object[]{"cream", 250, "ml"},
                new Object[]{"butter", 50, "g"});

        addRecipe(recipeDao, "Bunny Chow",
                "1. Cook the curry until the meat is tender and the sauce has reduced. "
                        + "2. Hollow out a quarter loaf of bread. "
                        + "3. Spoon the curry into the bread cavity. "
                        + "4. Serve with the bread lid and a grated carrot salad on the side.",
                new Object[]{"bread loaf", 1, "unit"},
                new Object[]{"chicken", 400, "g"},
                new Object[]{"curry powder", 2, "tbsp"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"potato", 2, "unit"});

        addRecipe(recipeDao, "Slow-Cooked Pot Stew",
                "1. Brown the meat in a heavy pot in batches. "
                        + "2. Layer in the vegetables on top without stirring. "
                        + "3. Add the stock, cover, and simmer low and slow for several hours. "
                        + "4. Serve straight from the pot with maize porridge or rice.",
                new Object[]{"beef", 500, "g"},
                new Object[]{"potato", 3, "unit"},
                new Object[]{"carrot", 2, "unit"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"beef stock", 500, "ml"});

        addRecipe(recipeDao, "Curried Lamb Skewers",
                "1. Marinate the meat overnight in the curry-and-apricot marinade. "
                        + "2. Thread the meat, onion and dried apricots onto skewers. "
                        + "3. Grill over hot coals, basting often with the marinade. "
                        + "4. Rest briefly before serving.",
                new Object[]{"lamb", 500, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"dried apricots", 100, "g"},
                new Object[]{"curry powder", 1, "tbsp"},
                new Object[]{"apricot jam", 2, "tbsp"});

        addRecipe(recipeDao, "Samp and Beans",
                "1. Soak the samp and beans together overnight. "
                        + "2. Simmer until soft, topping up with water as needed. "
                        + "3. Fry the onion and stir it through. "
                        + "4. Season well and serve hot.",
                new Object[]{"samp", 250, "g"},
                new Object[]{"sugar beans", 150, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"vegetable oil", 1, "tbsp"});

        addRecipe(recipeDao, "Spicy Bean Relish",
                "1. Sauté the onion, carrot and green pepper until softened. "
                        + "2. Stir in the baked beans, curry powder and chilli. "
                        + "3. Simmer until thickened. "
                        + "4. Serve warm as a relish alongside maize porridge or grilled meat.",
                new Object[]{"onion", 1, "unit"},
                new Object[]{"carrot", 2, "unit"},
                new Object[]{"green pepper", 1, "unit"},
                new Object[]{"baked beans", 1, "can"},
                new Object[]{"curry powder", 1, "tsp"});

        addRecipe(recipeDao, "Dried Beef and Cheese Board",
                "1. Slice the dried beef thinly against the grain. "
                        + "2. Arrange it with the cheese and crackers on a board. "
                        + "3. Add a small bowl of chutney for dipping. "
                        + "4. Serve at room temperature.",
                new Object[]{"dried beef", 100, "g"},
                new Object[]{"cheddar cheese", 150, "g"},
                new Object[]{"crackers", 12, "unit"},
                new Object[]{"chutney", 2, "tbsp"});

        addRecipe(recipeDao, "Grandma's Rusks",
                "1. Mix the dry ingredients, then work in the melted butter, eggs and buttermilk. "
                        + "2. Press the dough into a baking tin and bake until firm. "
                        + "3. Slice into fingers while still warm. "
                        + "4. Dry out overnight in a low oven until crisp.",
                new Object[]{"flour", 500, "g"},
                new Object[]{"sugar", 150, "g"},
                new Object[]{"butter", 250, "g"},
                new Object[]{"egg", 2, "unit"},
                new Object[]{"buttermilk", 250, "ml"});

        addRecipe(recipeDao, "Red Bush Iced Tea",
                "1. Steep the red bush tea bags in boiling water for 5 minutes. "
                        + "2. Stir in the sugar and honey while still hot. "
                        + "3. Add the lemon juice and top up with cold water. "
                        + "4. Chill and serve over ice.",
                new Object[]{"red bush tea bag", 4, "unit"},
                new Object[]{"water", 1, "l"},
                new Object[]{"sugar", 30, "g"},
                new Object[]{"lemon juice", 30, "ml"},
                new Object[]{"honey", 1, "tbsp"});

        addRecipe(recipeDao, "Pan-Fried Meatballs",
                "1. Soak the bread in milk, then mash it into the mince with the onion, egg and herbs. "
                        + "2. Shape into balls and flatten slightly. "
                        + "3. Fry in a hot pan until browned on both sides. "
                        + "4. Simmer in a light gravy until cooked through.",
                new Object[]{"beef mince", 500, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"egg", 1, "unit"},
                new Object[]{"bread", 2, "slice"},
                new Object[]{"milk", 50, "ml"});

        addRecipe(recipeDao, "Peppermint Crisp Tart",
                "1. Whip the cream until soft peaks form and fold in half the caramel treat. "
                        + "2. Layer the biscuits and cream mixture in a dish, repeating to fill it. "
                        + "3. Grate the peppermint chocolate over the top layer. "
                        + "4. Chill for at least four hours before serving.",
                new Object[]{"tennis biscuits", 200, "g"},
                new Object[]{"caramel treat", 1, "can"},
                new Object[]{"cream", 500, "ml"},
                new Object[]{"peppermint chocolate", 3, "unit"});

        addRecipe(recipeDao, "Pumpkin Fritters",
                "1. Mash the cooked pumpkin and beat in the eggs. "
                        + "2. Fold in the flour, sugar and cinnamon to make a thick batter. "
                        + "3. Fry spoonfuls in hot oil until golden on both sides. "
                        + "4. Drain and dust with extra cinnamon sugar.",
                new Object[]{"pumpkin", 400, "g"},
                new Object[]{"flour", 150, "g"},
                new Object[]{"egg", 2, "unit"},
                new Object[]{"sugar", 2, "tbsp"},
                new Object[]{"cinnamon", 1, "tsp"});

        addRecipe(recipeDao, "Grilled Cheese and Tomato Sandwiches",
                "1. Butter the bread on the outside of each slice. "
                        + "2. Layer cheese, sliced tomato and onion between two slices. "
                        + "3. Grill over the coals on both sides until the bread toasts and the cheese melts. "
                        + "4. Cut in half and serve hot.",
                new Object[]{"bread", 4, "slice"},
                new Object[]{"cheddar cheese", 100, "g"},
                new Object[]{"tomato", 1, "unit"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"butter", 1, "tbsp"});

        addRecipe(recipeDao, "Butternut Soup",
                "1. Sauté the onion, then add the cubed butternut and curry powder. "
                        + "2. Pour in the stock and simmer until the butternut is soft. "
                        + "3. Blend until smooth. "
                        + "4. Stir in the cream and season before serving.",
                new Object[]{"butternut", 800, "g"},
                new Object[]{"onion", 1, "unit"},
                new Object[]{"vegetable stock", 750, "ml"},
                new Object[]{"cream", 100, "ml"},
                new Object[]{"curry powder", 1, "tsp"});
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
