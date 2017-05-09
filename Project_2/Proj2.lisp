(defun concat (x y)
  (concatenate 'string x y))

(defun make-constructor (class-name class-attributes)
   `(defun ,(intern (concat "make-" (symbol-name class-name))) (class-name) class-attributes))

(defmacro def-class (class-name &rest class-attributes)
  (make-constructor class-name class-attributes))


 
