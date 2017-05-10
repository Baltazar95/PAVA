(defun concat (x y)
  (concatenate 'string x "-" y))

(defun make-constructor (class-name class-attributes)
  `(defun ,(intern (concat "MAKE" (symbol-name class-name))) (&key ,@class-attributes) (vector ,@class-attributes)))

(defun create-func (class-name x index)
  `(defun ,(intern (concat (symbol-name class-name) (symbol-name x)))
       (,class-name) (aref ,class-name ,index)))

(defun make-getters (class-name class-attributes)
    (loop for x in class-attributes
       do
	 (create-func class-name x 0)))

(defmacro def-class (class-name &rest class-attributes)
  (progn (make-getters class-name class-attributes)
	 (make-constructor class-name class-attributes)))




