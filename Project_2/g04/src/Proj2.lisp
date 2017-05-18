(defvar hash (make-hash-table :test 'equal))

(defun make-constructor (class-name class-attributes)
  `(defun ,(intern (concatenate 'string "MAKE-" (symbol-name class-name)))
       (&key ,@class-attributes) (vector ,(symbol-name class-name) ,@class-attributes)))

(defun make-getter (class-name x)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "-" (symbol-name x))) (name)
     ;; Confirm if x is of type class-name
     (if (,(intern (concatenate 'string (symbol-name class-name) "?")) name)
	 (aref name
	       (+ 1 (position
		     ,(symbol-name x)
		     (mapcar #'(lambda (y) (symbol-name y))
			     (cdr (gethash (aref name 0) ,hash))) :test #'equal))))))

(defun make-setter (class-name x)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "-" (symbol-name x))) (name value)
     ;; Confirm if x is of type class-name
     (if (,(intern (concatenate 'string (symbol-name class-name) "?")) name)
	 (setf (aref name
	       (+ 1 (position
		     ,(symbol-name x)
		     (mapcar #'(lambda (y) (symbol-name y))
			     (cdr (gethash (aref name 0) ,hash))) :test #'equal))) value))))
     

(defun make-get-set (class-name class-attributes)
    (mapcar #'(lambda (x) (make-getter class-name x) (make-setter class-name x)) class-attributes))

(defun make-isinstance (class-name)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "?")) (x)
     ;; This function inside a function has the objective check all branches of inheritance to if x is of class class-name
     (not (listp (labels ((is-of-super (class name)
			   (cond ((equal name class) (return-from ,(intern (concatenate 'string (symbol-name class-name) "?")) T))
				 ((not (car (gethash name hash))) NIL)
				 (T (mapcar #'(lambda (y) (is-of-super class (symbol-name y))) (car (gethash name hash)))))))
			 (is-of-super ,(symbol-name class-name) (aref x 0)))))))

;;; This function is just for simplification of the code, so that we don't have lines too long
;;; when inserting the attributes for a given class in our hash structure
(defun insert-in-hash (class-name inheritance class-attributes)
  `(setf (gethash ,(symbol-name class-name) hash)
	 (append '(,inheritance) ',class-attributes)))

(defun get-inherited-attributes (inheritance)
  (mapcar #'(lambda (name) (cdr (gethash (symbol-name name) hash))) inheritance))

;;; The remove-parenthesis function has the objective to correct the encapsulated attributes lists that come from the get-inherited-attributes
(defun remove-parenthesis (lst)
  (if lst
      (append (car lst) (remove-parenthesis (cdr lst)))))      

(defmacro def-class (class-name &rest class-attributes)
  (if (listp class-name)
      (let ((name (nth 0 class-name))
	    (inheritance (cdr class-name))
	    ;; the attributes local variable will conatain the attributes of the class appended
	    ;; with the attributes of the superclass's
	    (attributes (append (remove-parenthesis (get-inherited-attributes (cdr class-name))) class-attributes)))
	`(progn ,(make-constructor name attributes)
		,(make-isinstance name)
		,@(make-get-set name attributes)
		,(insert-in-hash name inheritance attributes)))
      `(progn ,(make-constructor class-name class-attributes)
	      ,(make-isinstance class-name)
	      ,@(make-get-set class-name class-attributes)
	      ,(insert-in-hash class-name '() class-attributes))))



