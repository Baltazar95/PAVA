(defun concat (x y)
  (concatenate 'string x y))

(defun make-constructor (class-name class-attributes)
   `(defun ,(intern (concat "MAKE-" (symbol-name class-name))) (&key ,@class-attributes) (vector ,@class-attributes)))

(defmacro def-class (class-name &rest class-attributes)
  (make-constructor class-name class-attributes))
