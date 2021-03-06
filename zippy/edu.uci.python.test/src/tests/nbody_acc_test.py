# Oct 19, 2015
# numba-benchmarks
# https://github.com/numba/numba-benchmark
#
# originally by Antoine Pitrou
# modified by myq
# remove classes and add pure nbody python

"""
Benchmark an implementation of the N-body simulation.

As in the CUDA version, we only compute accelerations and don't care to
update speeds and positions.
"""

from __future__ import division

def run_nbody(n, positions, weights):
    for i in range(n):
        ax = 0.0
        ay = 0.0
        for j in range(n):
            rx = positions[j][0] - positions[i][0]
            ry = positions[j][1] - positions[i][1]
            sqr_dist = rx * rx + ry * ry + 1e-6
            sixth_dist = sqr_dist * sqr_dist * sqr_dist
            inv_dist_cube = 1.0 / (sixth_dist ** 0.5)
            s = weights[j] * inv_dist_cube
            ax += s * rx
            ay += s * ry
        accelerations[i][0] = ax
        accelerations[i][1] = ay

n_bodies = 16

positions = [[0.76805746460138, -0.5228139230316793], [-0.5991956149660083, -0.47555504642935853],
            [-0.7020359542044337, 0.3038111469357141], [-0.34670159144966406, 0.31014135724965075],
            [-0.10826187533509635, 0.6230733903479946], [0.3822781879247321, -0.08939168547494036],
            [0.34063510574388944, -0.6649603911271325], [0.1725668224080743, 0.011714728165011623],
            [0.8042952709018529, 0.2848322515728883], [0.09994722560095437, 0.093823184499354],
            [-0.6269597093918231, -0.8423311136485288], [-0.6132558984588423, 0.932848373509682],
            [0.7641438030155772, 0.41481249329138015], [0.08447688106204598, -0.7544715676685332],
            [0.345008023967718, 0.4345562265505478], [0.17280505970356752, -0.24661570490474816]]

weights = [1.9796910127152212, 1.8793469121522173, 1.389219866350441, 1.4787098745714768,
            1.4392298363460903, 1.5718778884818412, 1.5473247242143708, 1.4978470382961593,
            1.3034495515007873, 1.4888096243800564, 1.010720089891421, 1.0492383262672078,
            1.1222540101360987, 1.7729637404264191, 1.274221952886791, 1.1803853964002908]

accelerations = [[0.0 for i in range(2)] for j in range(n_bodies)]

def time_nbody():
    run_nbody(n_bodies, positions, weights)


time_nbody()
print(accelerations)
