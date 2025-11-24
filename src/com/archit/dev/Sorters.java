package com.archit.dev;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Sorters {

    public static <T> void mergeSort(List<T> list, Comparator<T> comparator) {
        if (list.size() < 2) {
            return;
        }

        int mid = list.size() / 2;
        List<T> leftHalf = new ArrayList<>(list.subList(0, mid));
        List<T> rightHalf = new ArrayList<>(list.subList(mid, list.size()));

        mergeSort(leftHalf, comparator);
        mergeSort(rightHalf, comparator);

        merge(list, leftHalf, rightHalf, comparator);
    }

    private static <T> void merge(List<T> originalList, List<T> leftHalf, List<T> rightHalf, Comparator<T> comparator) {
        int i = 0;
        int j = 0;
        int k = 0;

        while (i < leftHalf.size() && j < rightHalf.size()) {
            if (comparator.compare(leftHalf.get(i), rightHalf.get(j)) <= 0) {
                originalList.set(k, leftHalf.get(i));
                i++;
            } else {
                originalList.set(k, rightHalf.get(j));
                j++;
            }
            k++;
        }

        while (i < leftHalf.size()) {
            originalList.set(k, leftHalf.get(i));
            i++;
            k++;
        }

        while (j < rightHalf.size()) {
            originalList.set(k, rightHalf.get(j));
            j++;
            k++;
        }
    }
}