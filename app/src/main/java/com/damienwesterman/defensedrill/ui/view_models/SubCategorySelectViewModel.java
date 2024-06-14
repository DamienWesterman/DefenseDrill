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

import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.List;

/**
 * TODO Doc comments
 * Abstraction so that we don't do another database call on phone rotation
 */
public class SubCategorySelectViewModel extends AndroidViewModel {
    private List<SubCategoryEntity> categories;
    private final DrillRepository repo;

    public SubCategorySelectViewModel(Application application) {
        super(application);

        repo = DrillRepository.getInstance(application);
        categories = null;
    }

    public List<SubCategoryEntity> getCategories(long id) {
        if (null == categories) {
            if (Constants.RANDOM_CHOICE == id) {
                categories = repo.getAllSubCategories();
            } else {
                categories = repo.getAllSubCategories(id);
            }
        }
        return this.categories;
    }
}
