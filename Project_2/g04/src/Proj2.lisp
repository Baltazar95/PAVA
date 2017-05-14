(defvar hash (make-hash-table :test 'equal))

(defun make-constructor (class-name class-attributes)
  `(defun ,(intern (concatenate 'string  "MAKE-" (symbol-name class-name))) (&key ,@class-attributes) (vector ,(symbol-name class-name) ,@class-attributes)))

(defun make-getter (class-name x)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "-" (symbol-name x))) (name) 
     (if (,(intern (concatenate 'string (symbol-name class-name) "?")) name)
	  (aref name (+ 1 (position ,(symbol-name x) (mapcar #'(lambda (y) (symbol-name y)) (cdr (gethash (aref name 0) ,hash))) :test #'equal))))))

(defun make-getters (class-name class-attributes)
    (mapcar
     #'(lambda (x) (make-getter class-name x)) class-attributes))

(defun make-isinstance (class-name)
  `(defun ,(intern (concatenate 'string (symbol-name class-name) "?")) (x)
     (not (listp (labels ((is-of-super (class name)
			   (cond ((equal name class) (return-from ,(intern (concatenate 'string (symbol-name class-name) "?")) T))
				 ((not (car (gethash name hash))) NIL)
				 (T (mapcar #'(lambda (y) (is-of-super class (symbol-name y))) (car (gethash name hash)))))))
			 (is-of-super ,(symbol-name class-name) (aref x 0)))))))

(defun insert-in-hash (class-name inheritance class-attributes)
  `(setf (gethash ,(symbol-name class-name) hash)
	 (append '(,inheritance) ',class-attributes)))

(defun get-inherited-attributes (inheritance)
  (mapcar #'(lambda (name)
;	      (if (not (car (gethash (symbol-name name) hash)))
		  (cdr (gethash (symbol-name name) hash)))
;;;;;;;;;;; caar serve para irmos buscar só o primeiro elemento da herança
;		  (append (get-inherited-attributes (caar (gethash (symbol-name name) hash)))
;			  (cdr (gethash (symbol-name name) hash)))))
	  inheritance))

(defun remove-parenthesis (lst)
  (if lst
      (append (car lst) (remove-parenthesis (cdr lst)))))
      

(defmacro def-class (class-name &rest class-attributes)
  (if (listp class-name)
      (let ((name (nth 0 class-name)) (inheritance (cdr class-name)) (attributes (append (remove-parenthesis (get-inherited-attributes (cdr class-name))) class-attributes)))
	`(progn ,(make-constructor name attributes)
		,@(make-getters name attributes)
		,(make-isinstance name)
		,(insert-in-hash name inheritance attributes)))
      `(progn ,(make-constructor class-name class-attributes)
	      ,@(make-getters class-name class-attributes)
	      ,(make-isinstance class-name)
	      ,(insert-in-hash class-name '() class-attributes))))



