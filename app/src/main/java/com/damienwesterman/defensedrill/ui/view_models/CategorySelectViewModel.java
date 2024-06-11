/****************************\
 *      ________________      *
 *     /  _             \     *
 *     \   \ |\   _  \  /     *
 *      \  / | \ / \  \/      *
 *      /  \ | / | /  /\      *
 *     /  _/ |/  \__ /  \     *
 *     \________________/     *
 *                            *
 \****************************/

package com.damienwesterman.defensedrill.ui.view_models;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.DrillRepository;

import java.util.List;

/**
 * TODO Doc comments
 */
public class CategorySelectViewModel extends AndroidViewModel {
    private List<CategoryEntity> categories;
    private final DrillRepository repo;

    public CategorySelectViewModel(Application application) {
        super(application);

        repo = DrillRepository.getInstance(application);
        categories = null;
    }

    public List<CategoryEntity> getCategories() {
        if (null == categories) {
            categories = repo.getAllCategories();
        }
        return this.categories;
    }
}
