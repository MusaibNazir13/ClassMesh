package com.desk.classmesh.listeners;

import java.util.List;

/**
 * Listener interface for handling validation results from the Semester Configuration view.
 */
public interface ValidationListener {
    /**
     * Called when semester and subject input validation is successful.
     * @param subjects A list of all collected subject names.
     */
    void onValidationSuccess(List<String> subjects);

    /**
     * Called when semester and subject input validation fails.
     * @param errorMessage The reason for the validation failure.
     */
    void onValidationFailure(String errorMessage);
}