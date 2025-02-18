/*! \file
   This header defines utility functions to deal with complex numbers and
   complex polynomials.*/

void sincos(float theta, out float s, out float c) {
    s = sin(theta);
    c = cos(theta);
}

float saturate(float a) {
    return clamp(a, 0., 1.);
}


/*! Returns the complex conjugate of the given complex number (i.e. it changes
	the sign of the y-component).*/
vec2 Conjugate(vec2 Z){
    return vec2(Z.x, -Z.y);
}
/*! This function implements complex multiplication.*/
vec2 Multiply(vec2 LHS, vec2 RHS){
    return vec2(LHS.x*RHS.x-LHS.y*RHS.y, LHS.x*RHS.y+LHS.y*RHS.x);
}
/*! This function computes the magnitude of the given complex number.*/
float Magnitude(vec2 Z){
    return sqrt(dot(Z, Z));
}
/*! This function computes the quotient of two complex numbers. The denominator
	must not be zero.*/
vec2 Divide(vec2 Numerator, vec2 Denominator){
    return vec2(Numerator.x*Denominator.x+Numerator.y*Denominator.y, -Numerator.x*Denominator.y+Numerator.y*Denominator.x)/dot(Denominator, Denominator);
}
/*! This function divides a real number by a complex number. The denominator
	must not be zero.*/
vec2 Divide(float Numerator, vec2 Denominator){
    return vec2(Numerator*Denominator.x, -Numerator*Denominator.y)/dot(Denominator, Denominator);
}
/*! This function implements computation of the reciprocal of the given non-
	zero complex number.*/
vec2 Reciprocal(vec2 Z){
    return vec2(Z.x, -Z.y)/dot(Z, Z);
}
/*! This utility function implements complex squaring.*/
vec2 Square(vec2 Z){
    return vec2(Z.x*Z.x-Z.y*Z.y, 2.0f*Z.x*Z.y);
}
/*! This utility function implements complex computation of the third power.*/
vec2 Cube(vec2 Z){
    return Multiply(Square(Z), Z);
}
/*! This utility function computes one square root of the given complex value.
	The other one can be found using the unary minus operator.
  \warning This function is continuous but not defined on the negative real
			axis (and cannot be continued continuously there).
  \sa SquareRoot() */
vec2 SquareRootUnsafe(vec2 Z){
    float ZLengthSq=dot(Z, Z);
    float ZLengthInv=inversesqrt(ZLengthSq);
    vec2 UnnormalizedRoot=Z*ZLengthInv+vec2(1.0f, 0.0f);
    float UnnormalizedRootLengthSq=dot(UnnormalizedRoot, UnnormalizedRoot);
    float NormalizationFactorInvSq=UnnormalizedRootLengthSq*ZLengthInv;
    float NormalizationFactor=inversesqrt(NormalizationFactorInvSq);
    return NormalizationFactor*UnnormalizedRoot;
}
/*! This utility function computes one square root of the given complex value.
	The other one can be found using the unary minus operator.
  \note This function has discontinuities for values with real part zero.
  \sa SquareRootUnsafe() */
vec2 SquareRoot(vec2 Z){
    vec2 ZPositiveRealPart=vec2(abs(Z.x), Z.y);
    vec2 ComputedRoot=SquareRootUnsafe(ZPositiveRealPart);
    return (Z.x>=0.0)?ComputedRoot:ComputedRoot.yx;
}
/*! This utility function computes one cubic root of the given complex value. The
   other roots can be found by multiplication by cubic roots of unity.
  \note This function has various discontinuities.*/
vec2 CubicRoot(vec2 Z){
    float Argument=atan(Z.y, Z.x);
    float NewArgument=Argument/3.0f;
    vec2 NormalizedRoot;
    sincos(NewArgument, NormalizedRoot.y, NormalizedRoot.x);
    return NormalizedRoot*pow(dot(Z, Z), 1.0f/6.0f);
}

/*! @{
   Returns the complex conjugate of the given complex vector (i.e. it changes the
   second column resp the y-component).*/
mat2x2 Conjugate(mat2x2 Vector){
    return mat2x2(Vector[0].x, -Vector[0].y, Vector[1].x, -Vector[1].y);
}
mat3x2 Conjugate(mat3x2 Vector){
    return mat3x2(Vector[0].x, -Vector[0].y, Vector[1].x, -Vector[1].y, Vector[2].x, -Vector[2].y);
}
mat4x2 Conjugate(mat4x2 Vector){
    return mat4x2(Vector[0].x, -Vector[0].y, Vector[1].x, -Vector[1].y, Vector[2].x, -Vector[2].y, Vector[3].x, -Vector[3].y);
}
void Conjugate(out vec2 OutConjugateVector[5], vec2 Vector[5]){
    for (int i=0;i!=5;++i){
        OutConjugateVector[i]=vec2(Vector[i].x, -Vector[i].x);
    }
}
//!@}

/*! Returns the real part of a complex number as real.*/
float RealPart(vec2 Z){
    return Z.x;
}

/*! Given coefficients of a quadratic polynomial A*x^2+B*x+C, this function
	outputs its two complex roots.*/
void SolveQuadratic(out vec2 pOutRoot[2], vec2 A, vec2 B, vec2 C)
{
    // Normalize the coefficients
    vec2 InvA=Reciprocal(A);
    B=Multiply(B, InvA);
    C=Multiply(C, InvA);
    // Divide the middle coefficient by two
    B*=0.5f;
    // Apply the quadratic formula
    vec2 DiscriminantRoot=SquareRoot(Square(B)-C);
    pOutRoot[0]=-B-DiscriminantRoot;
    pOutRoot[1]=-B+DiscriminantRoot;
}

/*! Given coefficients of a cubic polynomial A*x^3+B*x^2+C*x+D, this function
	outputs its three complex roots.*/
void SolveCubicBlinn(out vec2 pOutRoot[3], vec2 A, vec2 B, vec2 C, vec2 D)
{
    // Normalize the polynomial
    vec2 InvA=Reciprocal(A);
    B=Multiply(B, InvA);
    C=Multiply(C, InvA);
    D=Multiply(D, InvA);
    // Divide middle coefficients by three
    B/=3.0f;
    C/=3.0f;
    // Compute the Hessian and the discriminant
    vec2 Delta00=-Square(B)+C;
    vec2 Delta01=-Multiply(C, B)+D;
    vec2 Delta11=Multiply(B, D)-Square(C);
    vec2 Discriminant=4.0f*Multiply(Delta00, Delta11)-Square(Delta01);
    // Compute coefficients of the depressed cubic
    // (third is zero, fourth is one)
    vec2 DepressedD=-2.0f*Multiply(B, Delta00)+Delta01;
    vec2 DepressedC=Delta00;
    // Take the cubic root of a complex number avoiding cancellation
    vec2 DiscriminantRoot=SquareRoot(-Discriminant);
    DiscriminantRoot=faceforward(DiscriminantRoot, DiscriminantRoot, DepressedD);
    vec2 CubedRoot=DiscriminantRoot-DepressedD;
    vec2 FirstRoot=CubicRoot(0.5f*CubedRoot);
    vec2 pCubicRoot[3]={
    FirstRoot,
    Multiply(vec2(-0.5f, -0.5f*sqrt(3.0f)), FirstRoot),
    Multiply(vec2(-0.5f, 0.5f*sqrt(3.0f)), FirstRoot)
    };
    // Also compute the reciprocal cubic roots
    vec2 InvFirstRoot=Reciprocal(FirstRoot);
    vec2 pInvCubicRoot[3]={
    InvFirstRoot,
    Multiply(vec2(-0.5f, 0.5f*sqrt(3.0f)), InvFirstRoot),
    Multiply(vec2(-0.5f, -0.5f*sqrt(3.0f)), InvFirstRoot)
    };
    // Turn them into roots of the depressed cubic and revert the depression
    // transform

    for (int i=0;i!=3;++i)
    {
        pOutRoot[i]=pCubicRoot[i]-Multiply(DepressedC, pInvCubicRoot[i])-B;
    }
}


/*! Given coefficients of a quartic polynomial A*x^4+B*x^3+C*x^2+D*x+E, this
	function outputs its four complex roots.*/
void SolveQuarticNeumark(out vec2 pOutRoot[4], vec2 A, vec2 B, vec2 C, vec2 D, vec2 E)
{
    // Normalize the polynomial
    vec2 InvA=Reciprocal(A);
    B=Multiply(B, InvA);
    C=Multiply(C, InvA);
    D=Multiply(D, InvA);
    E=Multiply(E, InvA);
    // Construct a normalized cubic
    vec2 P=-2.0f*C;
    vec2 Q=Square(C)+Multiply(B, D)-4.0f*E;
    vec2 R=Square(D)+Multiply(Square(B), E)-Multiply(Multiply(B, C), D);
    // Compute a root that is not the smallest of the cubic
    vec2 pCubicRoot[3];
    SolveCubicBlinn(pCubicRoot, vec2(1.0f, 0.0f), P, Q, R);
    vec2 y=(dot(pCubicRoot[1], pCubicRoot[1])>dot(pCubicRoot[0], pCubicRoot[0]))?pCubicRoot[1]:pCubicRoot[0];

    // Solve a quadratic to obtain linear coefficients for quadratic polynomials
    vec2 BB=Square(B);
    vec2 fy=4.0f*y;
    vec2 BB_fy=BB-fy;
    vec2 tmp=SquareRoot(BB_fy);
    vec2 G=(B+tmp)*0.5f;
    vec2 g=(B-tmp)*0.5f;
    // Construct the corresponding constant coefficients
    vec2 Z=C-y;
    tmp=Divide(0.5f*Multiply(B, Z)-D, tmp);
    vec2 H=Z*0.5f+tmp;
    vec2 h=Z*0.5f-tmp;

    // Compute the roots
    vec2 pQuadraticRoot[2];
    SolveQuadratic(pQuadraticRoot, vec2(1.0f, 0.0f), G, H);
    pOutRoot[0]=pQuadraticRoot[0];
    pOutRoot[1]=pQuadraticRoot[1];
    SolveQuadratic(pQuadraticRoot, vec2(1.0f, 0.0f), g, h);
    pOutRoot[2]=pQuadraticRoot[0];
    pOutRoot[3]=pQuadraticRoot[1];
}
