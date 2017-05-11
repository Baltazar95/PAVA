(defun make-constructor (class-name class-attributes)
  `(defun ,(intern (concatenate 'string  "MAKE-" (symbol-name class-name))) (&key ,@class-attributes) (vector ,(symbol-name class-name) ,@class-attributes)))

(defun make-getter (class-name x index)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "-" (symbol-name x)))
       (,class-name) (aref ,class-name ,index)))

(defun make-getters (class-name class-attributes)
  (let ((index 0))
    (mapcar
     #'(lambda (x) (make-getter class-name x (incf index))) class-attributes)))

(defun make-isinstance (class-name)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "?"))
       (x) (eq (aref x 0) ,(symbol-name class-name))))

(defun make-getfields (class-name class-attributes)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "-FIELDS")) (,class-name) ',class-attributes)) 

(defmacro def-class (class-name &rest class-attributes)
  (if (listp class-name)
      (let ((name (nth 0 class-name))
	    (attributes (append ((intern (concatenate 'string (symbol-name (nth 1 class-name)) "-FIELDS")))) class-attributes))
      `(progn ,(make-constructor name attributes)
	      ,@(make-getters name attributes)
	      ,(make-isinstance name)
	      ,(make-getfields name attributes)))
  
      `(progn ,(make-constructor class-name class-attributes)
	      ,@(make-getters class-name class-attributes)
	      ,(make-isinstance class-name)
	      ,(make-getfields class-name class-attributes))))
;



