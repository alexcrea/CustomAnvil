package xyz.alexcrea.cuanvil.gui.util;

import xyz.alexcrea.cuanvil.gui.ValueUpdatableGui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.function.Consumer;

public class UpdateGuiStorage<T> {

     private final ArrayList<WeakReference<T>> stored = new ArrayList<>();

     public void add(T value) {
          stored.add(new WeakReference<>(value));
     }

     public void forAll(Consumer<T> consumer) {
          var toRemove = new ArrayList<WeakReference<T>>();
          for(WeakReference<T> ref : stored) {
               var value = ref.get();
               if(value == null) {
                    toRemove.add(ref);
                    return;
               }

               consumer.accept(value);
          }

          stored.removeAll(toRemove);
     }
}
